package com.github.dactiv.saas.middleware.consumer;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.rabbitmq.client.Channel;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.service.convert.AbstractMinioAmqpFileConvertResolver;
import com.github.dactiv.saas.middleware.service.office.OfficeResolver;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * office 文件转换消费者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class OfficeFileConvertConsumer extends BasicFileConvertConsumer{

    public static final String DEFAULT_QUEUE_NAME = "cmis.middleware.office.file.convert";

    private final List<OfficeResolver> officeResolvers;

    public OfficeFileConvertConsumer(ObjectProvider<OfficeResolver> officeResolvers,
                                     AmqpTemplate amqpTemplate,
                                     RedissonClient redissonClient,
                                     ApplicationConfig applicationConfig) {
        super(applicationConfig, redissonClient, amqpTemplate);
        this.officeResolvers = officeResolvers.orderedStream().collect(Collectors.toList());
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

        try {

            Optional<OfficeResolver> optional = officeResolvers.stream().filter(r -> r.isSupport(meta)).findFirst();
            if (optional.isEmpty()) {
                log.warn("找不到支持 [" + meta + "] 的解析器");
                channel.basicNack(tag, false, false);
                return ;
            }

            RestResult<Map<String, Object>> result = optional.get().convert(meta);

            if (HttpStatus.OK.value() != result.getStatus()) {
                log.warn("ID 为 [" + meta.getId() + "] 的文件转换元数据执行转换失败", new SystemException(result.getMessage()));
                channel.basicNack(tag, false, false);
                return;
            }
            AbstractMinioAmqpFileConvertResolver.sendNoticeMessage(meta, amqpTemplate, result);
            channel.basicAck(tag, false);
        } finally {
            getBucket(id).deleteAsync();
        }

    }
}
