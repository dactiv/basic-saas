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
 * ???????????????????????????
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
            throw new SystemException("????????????????????? [" + targetFolder + "] ??????");
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

        log.info("????????? [" + outputFile  + "] ????????????????????? [" + fileObject.getBucketName() + "] ?????????.");
        RestResult<Map<String, Object>> result = configServiceFeignClient.singleUploadFile(
                multipartFile,
                fileObject.getBucketName(),
                Map.of()
        );

        try {
            log.info("?????? [" + file + "] ?????????");
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            log.warn("??????" + file + "???????????????", e);
        }

        meta.getMeta().putAll(result.getData());
        AbstractMinioAmqpFileConvertResolver.sendNoticeMessage(
                meta,
                amqpTemplate,
                RestResult.ofSuccess("???????????? [" + meta.getFile() + "] ??????", meta.getMeta())
        );

        channel.basicAck(tag, false);
    }

    public void convertVideo(String sourceFile, String outputFile, String format) throws Exception {
        FFmpegLogCallback.set();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(sourceFile)) {

            log.info("???????????????????????????");

            // ????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????true???????????????avformat_find_stream_info?????????????????????????????????AVFormatContext?????????????????????oc???
            grabber.start(true);

            log.info("???????????????????????????");

            // grabber.start???????????????????????????????????????????????????grabber???????????????oc???
            AVFormatContext avformatcontext = grabber.getFormatContext();

            // ????????????????????????????????????????????????+????????????
            int streamNum = avformatcontext.nb_streams();

            // ?????????????????????????????????
            if (streamNum < 1) {
                throw new SystemException("Error! There is no media stream in the file!");
            }

            // ?????????????????????
            int framerate = (int) grabber.getVideoFrameRate();

            log.info("???????????? [" + framerate + "]???????????????[" + avformatcontext.duration() / 1000000 + "]?????????????????????[" + avformatcontext.nb_streams() + "]");

            // ????????????????????????????????????
            for (int i = 0; i < streamNum; i++) {
                AVStream avstream = avformatcontext.streams(i);
                AVCodecParameters avcodecparameters = avstream.codecpar();
                log.info("????????????[" + i + "]??????????????????[" + avcodecparameters.codec_type() + "]????????????ID[" + avcodecparameters.codec_id() + "]");
            }

            // ????????????
            int frameWidth = grabber.getImageWidth();
            // ????????????
            int frameHeight = grabber.getImageHeight();
            // ??????????????????
            int audioChannels = grabber.getAudioChannels();

            log.info("????????????[" + frameWidth + "]???????????????[" + frameHeight + "]??????????????????[" + audioChannels + "]");
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, frameWidth, frameHeight, audioChannels)) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

                recorder.setFormat(format);
                // ????????????????????????????????????????????????????????????
                recorder.setVideoBitrate(grabber.getVideoBitrate());

                // ???????????????????????????
                recorder.setFrameRate(framerate);

                // ??????????????????????????????
                recorder.setGopSize(framerate);

                // ??????????????????????????????????????????????????????
                recorder.setAudioChannels(grabber.getAudioChannels());

                recorder.start();

                int videoFrameNum = 0;
                int audioFrameNum = 0;
                int dataFrameNum = 0;

                Frame frame;
                // ????????????????????????
                while (null != (frame = grabber.grab())) {
                    // ?????????????????????????????????
                    if (null != frame.image) {
                        videoFrameNum++;
                        // ???????????????????????????????????????
                        recorder.record(frame);
                    }

                    // ?????????????????????????????????
                    if (null != frame.samples) {
                        audioFrameNum++;
                        // ???????????????????????????????????????
                        recorder.record(frame);
                    }

                    // ?????????????????????????????????
                    if (null != frame.data) {
                        dataFrameNum++;
                    }
                }
                log.info("????????????????????????[" + videoFrameNum + "]????????????[" + audioFrameNum + "]????????????[" + dataFrameNum + "]");
            }
        }
    }
}
