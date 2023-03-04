package com.github.dactiv.saas.middleware.service.convert;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.enumeration.FileFromTypeEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Objects;

/**
 * 抽象的 minio 文件转换解析实现
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class AbstractMinioAmqpFileConvertResolver implements FileConvertResolver{

    @Getter
    private final AmqpTemplate amqpTemplate;

    public AbstractMinioAmqpFileConvertResolver(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public boolean isSupport(FileConvertMeta meta) {
        return FileFromTypeEnum.MINIO.equals(meta.getFormType());
    }

    public static void sendNoticeMessage(FileConvertMeta meta, AmqpTemplate amqpTemplate, Object result) {
        Object queueName = meta.getMeta().get(SystemConstants.NOTICE_MESSAGE_QUEUE_NAME);

        if (Objects.nonNull(queueName) && StringUtils.isNotEmpty(queueName.toString())) {
            Object exchangeName = meta.getMeta().get(SystemConstants.NOTICE_MESSAGE_EXCHANGE_NAME);
            if (Objects.nonNull(exchangeName) && StringUtils.isNotEmpty(exchangeName.toString())) {
                amqpTemplate.convertAndSend(
                        exchangeName.toString(),
                        queueName.toString(),
                        result
                );
                log.info("发送消息到 [" + exchangeName + "] 交换机的 [" + queueName + "] 队列, 数据数据内容为:" + Casts.writeValueAsString(result));
            } else {
                amqpTemplate.convertAndSend(
                        queueName.toString(),
                        result
                );
                log.info("发送消息到 [" + queueName + "] 队列, 数据数据内容为:" + Casts.writeValueAsString(result));
            }
        }
    }
}
