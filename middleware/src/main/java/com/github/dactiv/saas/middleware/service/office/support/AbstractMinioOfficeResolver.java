package com.github.dactiv.saas.middleware.service.office.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.enumeration.FileConvertTypeEnum;
import com.github.dactiv.saas.commons.enumeration.FileFromTypeEnum;
import com.github.dactiv.saas.commons.enumeration.OfficeFileTypeEnum;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.consumer.PdfFileConvertConsumer;
import com.github.dactiv.saas.middleware.service.office.OfficeResolver;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 抽象的 minio ofiice 解析器实现
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class AbstractMinioOfficeResolver implements OfficeResolver, InitializingBean {

    @Getter
    private final ConfigServiceFeignClient configServiceFeignClient;

    @Getter
    private final ApplicationConfig applicationConfig;

    private final PdfFileConvertConsumer pdfFileConvertConsumer;

    public AbstractMinioOfficeResolver(ConfigServiceFeignClient configServiceFeignClient,
                                       ApplicationConfig applicationConfig,
                                       PdfFileConvertConsumer pdfFileConvertConsumer) {
        this.configServiceFeignClient = configServiceFeignClient;
        this.applicationConfig = applicationConfig;
        this.pdfFileConvertConsumer = pdfFileConvertConsumer;
    }

    @Override
    public boolean isSupport(FileConvertMeta meta) {
        if (!FileFromTypeEnum.MINIO.equals(meta.getFormType())) {
            return false;
        }

        OfficeFileTypeEnum fileType = OfficeFileTypeEnum.pare(meta.getFile());
        return Objects.nonNull(fileType) && isSupport(fileType);
    }

    protected abstract boolean isSupport(OfficeFileTypeEnum type);

    @Override
    public RestResult<Map<String, Object>> convert(FileConvertMeta meta) throws Exception {

        if (!meta.getMeta().containsKey(SystemConstants.MINIO_OBJECT_NAME) || !meta.getMeta().containsKey(SystemConstants.MINIO_BUCKET_NAME)) {
            throw new SystemException("meta 中缺少: " + SystemConstants.MINIO_OBJECT_NAME + " 和 " + SystemConstants.MINIO_BUCKET_NAME + "字段");
        }

        FileObject fileObject = FileObject.of(
                meta.getMeta().get(SystemConstants.MINIO_BUCKET_NAME).toString(),
                meta.getMeta().get(SystemConstants.MINIO_OBJECT_NAME).toString()
        );

        String objectName = StringUtils.substringBeforeLast(fileObject.getObjectName(), Casts.DEFAULT_DOT_SYMBOL);

        ActiveXComponent activeXComponent = null;
        Dispatch target = null;
        Dispatch dispatch = null;
        Map<String, Object> data = meta.getMeta();

        String downloadPath = StringUtils.appendIfMissing(applicationConfig.getOfficeDownloadPath(), File.separator);
        String targetFolder = downloadPath + System.currentTimeMillis() + File.separator;

        File file = new File(targetFolder);
        if (!file.exists() && !file.mkdirs()) {
            throw new SystemException("创建 office 目标文件夹 [" + targetFolder + "] 失败");
        }

        String pdfPath = targetFolder + applicationConfig.getConvertTargetName() + FileConvertTypeEnum.PDF.getValue();

        try {

            if (!configServiceFeignClient.isFileExist(fileObject.getBucketName(), fileObject.getObjectName())) {
                throw new SystemException(fileObject.getObjectName() + ", 在桶 [" + fileObject.getBucketName() + "] 中不存在");
            }
            log.info("开始下载 [" + fileObject.getObjectName() + "] 文件");
            byte[] input = configServiceFeignClient.getFile(fileObject.getBucketName(), fileObject.getObjectName());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
            String suffix = StringUtils.substringAfterLast(fileObject.getObjectName(), Casts.DEFAULT_DOT_SYMBOL);
            String sourceFile = targetFolder + applicationConfig.getDownloadSourceName() + Casts.DEFAULT_DOT_SYMBOL + suffix;
            FileOutputStream downloadFile = new FileOutputStream(sourceFile);
            IOUtils.copy(inputStream, downloadFile);
            downloadFile.flush();
            IOUtils.close(inputStream, downloadFile);

            OfficeFileTypeEnum fileType = OfficeFileTypeEnum.pare(meta.getFile());
            log.info("开始对 [" + fileObject.getObjectName()  + "] 文件解析成 " + fileType.getName());
            ComThread.InitMTA(true);
            activeXComponent = new ActiveXComponent(fileType.getActiveXComponentName());
            target = getDispatch(activeXComponent, fileType);
            dispatch = Dispatch.call(target, "Open", sourceFile).toDispatch();

            postDispatchOpen(dispatch, activeXComponent, fileType, pdfPath);

            String pdfObjectName = objectName + FileConvertTypeEnum.PDF.getValue();
            FileObject pdfObject = FileObject.of(fileObject.getBucketName(), pdfObjectName);

            FileInputStream pdfFileInput = new FileInputStream(pdfPath);

            MultipartFile multipartFile = new ConfigServiceFeignClient.MockMultipartFile(
                    pdfObject.getObjectName(),
                    pdfFileInput
            );
            log.info("开始对 [" + pdfObjectName  + "] 文件进行上传");
            RestResult<Map<String, Object>> result = configServiceFeignClient.singleUploadFile(
                    multipartFile,
                    pdfObject.getBucketName(),
                    new LinkedHashMap<>()
            );
            data.put(applicationConfig.getConvertTargetName(), result.getData());

        } catch (Exception e) {
            FileUtils.deleteDirectory(file);
            return RestResult.ofException(e);
        } finally {
            releaseResource(activeXComponent, dispatch, target);
            ComThread.Release();
            ComThread.quitMainSTA();
        }

        if (!FileConvertTypeEnum.PDF.equals(meta.getConvertType())) {
            FileObject object = FileObject.of(fileObject.getBucketName(), objectName);
            List<Map<String, Object>> result = pdfFileConvertConsumer.uploadImage(pdfPath, object, meta.getConvertType(), targetFolder);
            data.put(applicationConfig.getImageFolderName(), result);
        }

        try {
            log.info("删除 [" + file + "] 文件夹");
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            log.warn("删除" + file + "文件夹失败", e);
        }

        return RestResult.ofSuccess("转换文件 [" + meta.getFile() + "] 成功", data);
    }

    protected abstract void releaseResource(ActiveXComponent activeXComponent, Dispatch dispatch, Dispatch target);

    /**
     * 处理 Dispatch 打开后的操作
     *
     * @param dispatch Dispatch 对象
     * @param officeFileType 文件类型
     * @param targetFolder 目标文件夹
     */
    protected abstract void postDispatchOpen(Dispatch dispatch, ActiveXComponent activeXComponent, OfficeFileTypeEnum officeFileType, String targetFolder);

    /**
     * 获取 Dispatch
     *
     * @param activeXComponent activeX 空间
     *
     * @return Dispatch
     */
    protected Dispatch getDispatch(ActiveXComponent activeXComponent, OfficeFileTypeEnum fileType) {
        return activeXComponent.getProperty(fileType.getProperty()).toDispatch();
    }

    @Override
    public void afterPropertiesSet() {
        File file = new File(applicationConfig.getOfficeDownloadPath());
        if (!file.exists() && !file.mkdirs()) {
            throw new SystemException("创建 [" + applicationConfig.getOfficeDownloadPath() + "] 文件夹失败");
        }
    }
}
