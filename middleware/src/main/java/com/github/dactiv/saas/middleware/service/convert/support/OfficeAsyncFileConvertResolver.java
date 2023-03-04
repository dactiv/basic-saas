package com.github.dactiv.saas.middleware.service.convert.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.enumeration.OfficeFileTypeEnum;
import com.github.dactiv.saas.middleware.consumer.OfficeFileConvertConsumer;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * office 异步文件转换解析器
 *
 * @author maurice.chen
 */
@Component
public class OfficeAsyncFileConvertResolver extends AbstractMinioAmqpFileConvertResolver {

    public OfficeAsyncFileConvertResolver(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public boolean isSupport(FileConvertMeta meta) {
        OfficeFileTypeEnum fileType = OfficeFileTypeEnum.pare(meta.getFile());
        return super.isSupport(meta) && Objects.nonNull(fileType);
    }

    @Override
    public RestResult<Object> convert(FileConvertMeta meta) {
        getAmqpTemplate().convertAndSend(
                SystemConstants.SYS_MIDDLEWARE_RABBITMQ_EXCHANGE,
                OfficeFileConvertConsumer.DEFAULT_QUEUE_NAME,
                meta.getId(),
                message -> {
                    String id = OfficeFileConvertConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + meta.getId();
                    message.getMessageProperties().setMessageId(id);
                    message.getMessageProperties().setCorrelationId(meta.getId());
                    return message;
                }
        );
        return RestResult.ofProcessing("正在转换:" + meta.getFile(), RestResult.SUCCESS_EXECUTE_CODE);
    }
}
