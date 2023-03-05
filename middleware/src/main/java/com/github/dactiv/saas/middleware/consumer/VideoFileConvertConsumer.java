package com.github.dactiv.saas.middleware.consumer;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.web.query.condition.support.SimpleConditionParser;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 视频文件转换消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class VideoFileConvertConsumer extends BasicFileConvertConsumer{

    public static final String DEFAULT_QUEUE_NAME = "dactiv.saas.middleware.video.file.convert";

    private final ConfigServiceFeignClient configServiceFeignClient;

    public VideoFileConvertConsumer(ApplicationConfig applicationConfig,
                                    ConfigServiceFeignClient configServiceFeignClient,
                                    AmqpTemplate amqpTemplate,
                                    RedissonClient redissonClient) {
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
            return;
        }

        FileObject fileObject = FileObject.of(
                meta.getMeta().get(SystemConstants.MINIO_BUCKET_NAME).toString(),
                meta.getMeta().get(SystemConstants.MINIO_OBJECT_NAME).toString()
        );

        String downloadPath = StringUtils.appendIfMissing(applicationConfig.getVideoDownloadPath(), File.separator);
        String targetFolder = downloadPath + System.currentTimeMillis() + File.separator;

        File file = new File(targetFolder);
        if (!file.exists() && !file.mkdirs()) {
            throw new SystemException("创建目标文件夹 [" + targetFolder + "] 失败");
        }

        byte[] input = configServiceFeignClient.getFile(fileObject.getBucketName(), fileObject.getObjectName());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input);

        String filename = fileObject.getObjectName();
        if (meta.getMeta().containsKey(SystemConstants.MINIO_ORIGINAL_FILE_NAME)) {
            filename = meta.getMeta().get(SystemConstants.MINIO_ORIGINAL_FILE_NAME).toString();
        }

        String sourceFile = targetFolder + applicationConfig.getDownloadSourceName() + SimpleConditionParser.DEFAULT_FIELD_CONDITION_SEPARATORS + meta.getMeta().get(SystemConstants.MINIO_ORIGINAL_FILE_NAME).toString();
        FileOutputStream downloadFile = new FileOutputStream(sourceFile);
        IOUtils.copy(inputStream, downloadFile);
        downloadFile.flush();
        IOUtils.close(inputStream, downloadFile);

        String targetName = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL) + meta.getConvertType().getValue();
        String outputFile = targetFolder + applicationConfig.getConvertTargetName() + SimpleConditionParser.DEFAULT_FIELD_CONDITION_SEPARATORS + targetName;

        convertVideo(sourceFile, outputFile, meta.getConvertType().getValue());

        FileInputStream videoInput = new FileInputStream(outputFile);

        MultipartFile multipartFile = new ConfigServiceFeignClient.MockMultipartFile(targetName, videoInput);

        log.info("开始对 [" + outputFile  + "] 文件进行上传到 [" + fileObject.getBucketName() + "] 路径中.");
        RestResult<Map<String, Object>> result = configServiceFeignClient.singleUploadFile(
                multipartFile,
                fileObject.getBucketName(),
                Map.of()
        );

        try {
            log.info("删除 [" + file + "] 文件夹");
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            log.warn("删除" + file + "文件夹失败", e);
        }

        meta.getMeta().putAll(result.getData());
        AbstractMinioAmqpFileConvertResolver.sendNoticeMessage(
                meta,
                amqpTemplate,
                RestResult.ofSuccess("转换文件 [" + meta.getFile() + "] 成功", meta.getMeta())
        );

        channel.basicAck(tag, false);
    }

    public void convertVideo(String sourceFile, String outputFile, String format) throws Exception {
        FFmpegLogCallback.set();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(sourceFile)) {

            log.info("开始初始化帧抓取器");

            // 初始化帧抓取器，例如数据结构（时间戳、编码器上下文、帧对象等），
            // 如果入参等于true，还会调用avformat_find_stream_info方法获取流的信息，放入AVFormatContext类型的成员变量oc中
            grabber.start(true);

            log.info("帧抓取器初始化完成");

            // grabber.start方法中，初始化的解码器信息存在放在grabber的成员变量oc中
            AVFormatContext avformatcontext = grabber.getFormatContext();

            // 文件内有几个媒体流（一般是视频流+音频流）
            int streamNum = avformatcontext.nb_streams();

            // 没有媒体流就不用继续了
            if (streamNum < 1) {
                throw new SystemException("Error! There is no media stream in the file!");
            }

            // 取得视频的帧率
            int framerate = (int) grabber.getVideoFrameRate();

            log.info("视频帧率 [" + framerate + "]，视频时长[" + avformatcontext.duration() / 1000000 + "]秒，媒体流数量[" + avformatcontext.nb_streams() + "]");

            // 遍历每一个流，检查其类型
            for (int i = 0; i < streamNum; i++) {
                AVStream avstream = avformatcontext.streams(i);
                AVCodecParameters avcodecparameters = avstream.codecpar();
                log.info("流的索引[" + i + "]，编码器类型[" + avcodecparameters.codec_type() + "]，编码器ID[" + avcodecparameters.codec_id() + "]");
            }

            // 视频宽度
            int frameWidth = grabber.getImageWidth();
            // 视频高度
            int frameHeight = grabber.getImageHeight();
            // 音频通道数量
            int audioChannels = grabber.getAudioChannels();

            log.info("视频宽度[" + frameWidth + "]，视频高度[" + frameHeight + "]，音频通道数[" + audioChannels + "]");
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, frameWidth, frameHeight, audioChannels)) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

                recorder.setFormat(format);
                // 使用原始视频的码率，若需要则自行修改码率
                recorder.setVideoBitrate(grabber.getVideoBitrate());

                // 一秒内的帧数，帧率
                recorder.setFrameRate(framerate);

                // 两个关键帧之间的帧数
                recorder.setGopSize(framerate);

                // 设置音频通道数，与视频源的通道数相等
                recorder.setAudioChannels(grabber.getAudioChannels());

                recorder.start();

                int videoFrameNum = 0;
                int audioFrameNum = 0;
                int dataFrameNum = 0;

                Frame frame;
                // 持续从视频源取帧
                while (null != (frame = grabber.grab())) {
                    // 有图像，就把视频帧加一
                    if (null != frame.image) {
                        videoFrameNum++;
                        // 取出的每一帧，都保存到视频
                        recorder.record(frame);
                    }

                    // 有声音，就把音频帧加一
                    if (null != frame.samples) {
                        audioFrameNum++;
                        // 取出的每一帧，都保存到视频
                        recorder.record(frame);
                    }

                    // 有数据，就把数据帧加一
                    if (null != frame.data) {
                        dataFrameNum++;
                    }
                }
                log.info("转码完成，视频帧[" + videoFrameNum + "]，音频帧[" + audioFrameNum + "]，数据帧[" + dataFrameNum + "]");
            }
        }
    }
}
