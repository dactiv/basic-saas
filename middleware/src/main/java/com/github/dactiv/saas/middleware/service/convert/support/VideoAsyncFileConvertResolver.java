package com.github.dactiv.saas.middleware.service.convert.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.consumer.VideoFileConvertConsumer;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;


/**
 * 视频文件转换解析器实现
 *
 * @author maurice.chen
 */
@Component
public class VideoAsyncFileConvertResolver extends AbstractMinioAmqpFileConvertResolver {

    private final ApplicationConfig applicationConfig;

    public VideoAsyncFileConvertResolver(AmqpTemplate amqpTemplate, ApplicationConfig applicationConfig) {
        super(amqpTemplate);
        this.applicationConfig = applicationConfig;
    }

    @Override
    public boolean isSupport(FileConvertMeta meta) {
        return applicationConfig.getSupportVideoFileSuffixList().stream().anyMatch(s -> StringUtils.endsWithIgnoreCase(meta.getFile(), s));
    }

    @Override
    public RestResult<Object> convert(FileConvertMeta meta) {
        getAmqpTemplate().convertAndSend(
                SystemConstants.SYS_MIDDLEWARE_RABBITMQ_EXCHANGE,
                VideoFileConvertConsumer.DEFAULT_QUEUE_NAME,
                meta.getId(),
                message -> {
                    String id = VideoFileConvertConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + meta.getId();
                    message.getMessageProperties().setMessageId(id);
                    message.getMessageProperties().setCorrelationId(meta.getId());
                    return message;
                }
        );
        return RestResult.ofProcessing("正在转换:" + meta.getFile(), RestResult.SUCCESS_EXECUTE_CODE);
    }
}
