package com.github.dactiv.saas.middleware.service.convert.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.consumer.PdfFileConvertConsumer;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * pdf 一步文件转换解析器
 *
 * @author maurice.chen
 */
@Component
public class PdfAsyncFileConvertResolver extends AbstractMinioAmqpFileConvertResolver {
    public static final String SUFFIX = ".pdf";

    @Override
    public boolean isSupport(FileConvertMeta meta) {
        return super.isSupport(meta) && StringUtils.endsWith(meta.getFile(), SUFFIX);
    }

    public PdfAsyncFileConvertResolver(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public RestResult<Object> convert(FileConvertMeta meta) {
        getAmqpTemplate().convertAndSend(
                SystemConstants.SYS_MIDDLEWARE_RABBITMQ_EXCHANGE,
                PdfFileConvertConsumer.DEFAULT_QUEUE_NAME,
                meta.getId(),
                message -> {
                    String id = PdfFileConvertConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + meta.getId();
                    message.getMessageProperties().setMessageId(id);
                    message.getMessageProperties().setCorrelationId(meta.getId());
                    return message;
                }
        );
        return RestResult.ofProcessing("正在转换:" + meta.getFile(), RestResult.SUCCESS_EXECUTE_CODE);
    }
}
