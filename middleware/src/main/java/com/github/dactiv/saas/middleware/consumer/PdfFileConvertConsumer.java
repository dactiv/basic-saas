package com.github.dactiv.saas.middleware.consumer;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.web.query.condition.support.SimpleConditionParser;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.enumeration.FileConvertTypeEnum;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import com.github.dactiv.saas.middleware.service.convert.support.PdfAsyncFileConvertResolver;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * pdf 文件转换消费者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class PdfFileConvertConsumer extends BasicFileConvertConsumer {

    public static final String DEFAULT_QUEUE_NAME = "cmis.middleware.pdf.file.convert";

    public static final String ORDER_NAME = "order";

    public static final String PREFIX_PARAM_NAME = "prefix";

    private final ConfigServiceFeignClient configServiceFeignClient;

    public PdfFileConvertConsumer(ApplicationConfig applicationConfig,
                                  AmqpTemplate amqpTemplate,
                                  RedissonClient redissonClient,
                                  ConfigServiceFeignClient configServiceFeignClient) {
        super(applicationConfig, redissonClient, amqpTemplate);
        this.configServiceFeignClient = configServiceFeignClient;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MIDDLEWARE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload String id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        FileConvertMeta meta = getFileConvertMeta(id, channel, tag);
        if (Objects.isNull(meta)) {
            return ;
        }

        FileObject fileObject = FileObject.of(
                meta.getMeta().get(SystemConstants.MINIO_BUCKET_NAME).toString(),
                meta.getMeta().get(SystemConstants.MINIO_OBJECT_NAME).toString()
        );

        String downloadPath = StringUtils.appendIfMissing(applicationConfig.getOfficeDownloadPath(), File.separator);
        String targetFolder = downloadPath + System.currentTimeMillis() + File.separator;

        File file = new File(targetFolder);
        if (!file.exists() && !file.mkdirs()) {
            throw new SystemException("创建 pdf 目标文件夹 [" + targetFolder + "] 失败");
        }

        try {
            byte[] input = configServiceFeignClient.getFile(fileObject.getBucketName(), fileObject.getObjectName());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input);

            String sourceFile = targetFolder + applicationConfig.getDownloadSourceName() + PdfAsyncFileConvertResolver.SUFFIX;
            FileOutputStream downloadFile = new FileOutputStream(sourceFile);
            IOUtils.copy(inputStream, downloadFile);
            downloadFile.flush();
            IOUtils.close(inputStream, downloadFile);

            String objectName = StringUtils.substringBeforeLast(fileObject.getObjectName(), Casts.DEFAULT_DOT_SYMBOL);
            FileObject object = FileObject.of(fileObject.getBucketName(), objectName);
            List<Map<String, Object>> result = uploadImage(sourceFile, object, meta.getConvertType(), targetFolder);

            meta.getMeta().put(applicationConfig.getImageFolderName(), result);
            AbstractMinioAmqpFileConvertResolver.sendNoticeMessage(
                    meta,
                    amqpTemplate,
                    RestResult.ofSuccess("转换文件 [" + meta.getFile() + "] 成功", meta.getMeta())
            );
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("转换 pdf 文件为图片发生错误", e);
        } finally {
            log.info("删除 [" + file + "] 文件夹");
            FileUtils.deleteDirectory(file);
            getBucket(id).deleteAsync();
        }
    }

    public List<Map<String, Object>> uploadImage(String path, FileObject fileObject, FileConvertTypeEnum type, String outputFolder) throws Exception {

        PDDocument document = PDDocument.load(new File(path));
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        String prefix = fileObject.getObjectName() + SimpleConditionParser.DEFAULT_FIELD_CONDITION_SEPARATORS + applicationConfig.getImageFolderName();

        List<Map<String, Object>> result = new LinkedList<>();
        log.info("对 [" + path + "] 文件解析成 " + type.getName() + " 图片, 共: " + document.getNumberOfPages() + " 长");
        for (int i = 0; i < document.getNumberOfPages(); i++) {

            BufferedImage image = pdfRenderer.renderImageWithDPI(i, applicationConfig.getPdfRenderImageDpi());
            Integer order = (i + 1);

            String imageFileName = order + type.getValue();
            String outputPath = outputFolder + imageFileName;

            ImageIO.write(image, type.getName(), new File(outputPath));
            FileInputStream imageInput = new FileInputStream(outputPath);
            MultipartFile multipartFile = new ConfigServiceFeignClient.MockMultipartFile(imageFileName, imageInput);

            log.info("开始对 [" + imageFileName  + "] 文件进行上传到 [" + prefix + "] 路径中.");
            RestResult<Map<String, Object>> imageResult = configServiceFeignClient.singleUploadFile(
                    multipartFile,
                    fileObject.getBucketName(),
                    Map.of(PREFIX_PARAM_NAME, prefix)
            );
            imageResult.getData().put(ORDER_NAME, order);
            result.add(imageResult.getData());
        }

        document.close();

        return result;
    }
}
