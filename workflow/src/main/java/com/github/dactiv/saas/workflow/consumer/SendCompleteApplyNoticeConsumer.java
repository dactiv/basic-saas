package com.github.dactiv.saas.workflow.consumer;


import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.dto.workflow.AuditCompleteResultDto;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.AuditOperationTypeEnum;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * 完成流程申请消息消费者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class SendCompleteApplyNoticeConsumer {

    public static final String DEFAULT_QUEUE_NAME = "cmis.workflow.send.complete.apply.notice";

    private final AmqpTemplate amqpTemplate;

    private final ApplyService applyService;

    public SendCompleteApplyNoticeConsumer(AmqpTemplate amqpTemplate, ApplyService applyService) {
        this.amqpTemplate = amqpTemplate;
        this.applyService = applyService;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_WORKFLOW_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void sendMessage(@Payload Integer id,
                            Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        ApplyEntity apply = applyService.get(id);

        if (Objects.isNull(apply)){
            log.warn("找不到 ID 为 [" + id + "] 的审批申请信息");
            channel.basicNack(tag, false, false);
            return ;
        }

        if (ApplyFormTypeEnum.CUSTOM.equals(apply.getFormType())){
            log.info("ID 为 [" + id + "] 的审批申请信息的表单类型为:" + ApplyFormTypeEnum.CUSTOM + ", 不做处理");
            channel.basicNack(tag, false, false);
            return ;
        }

        if (MapUtils.isEmpty(apply.getApplyContent())) {
            log.info("ID 为 [" + id + "] 的审批申请信息的申请数据为空, 不做处理");
            return ;
        }

        AuditCompleteResultDto dto = new AuditCompleteResultDto();

        dto.setId(apply.getFormId());
        dto.setApplyId(apply.getId());
        dto.setFormType(apply.getFormType());
        dto.setStatus(apply.getStatus());
        dto.setOperationType(AuditOperationTypeEnum.AUDIT);

        Object queueName = apply.getApplyContent().get(SystemConstants.NOTICE_MESSAGE_QUEUE_NAME);

        if (Objects.nonNull(queueName) && StringUtils.isNotEmpty(queueName.toString())) {
            Object exchangeName = apply.getApplyContent().get(SystemConstants.NOTICE_MESSAGE_EXCHANGE_NAME);
            sendNoticeConsumer(exchangeName, queueName, dto, amqpTemplate);
        }

        channel.basicAck(tag, false);
    }

    public static void sendNoticeConsumer(Object exchangeName, Object queueName, BasicIdentification<?> dto, AmqpTemplate amqpTemplate) {
        if (Objects.nonNull(exchangeName) && StringUtils.isNotEmpty(exchangeName.toString())) {
            amqpTemplate.convertAndSend(
                    exchangeName.toString(),
                    queueName.toString(),
                    dto,
                    message -> {
                        String messageId = queueName + Casts.DEFAULT_DOT_SYMBOL + dto.getId();
                        message.getMessageProperties().setMessageId(messageId);
                        message.getMessageProperties().setCorrelationId(dto.getId().toString());
                        return message;
                    }
            );
        } else {
            amqpTemplate.convertAndSend(
                    queueName.toString(),
                    dto,
                    message -> {
                        String messageId = queueName + Casts.DEFAULT_DOT_SYMBOL + dto.getId();
                        message.getMessageProperties().setMessageId(messageId);
                        message.getMessageProperties().setCorrelationId(dto.getId().toString());
                        return message;
                    }
            );
        }
    }
}
