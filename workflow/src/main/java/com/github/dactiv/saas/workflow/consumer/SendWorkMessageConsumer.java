package com.github.dactiv.saas.workflow.consumer;

import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.workflow.config.ApplicationConfig;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalStatusEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发送代办工作消息
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class SendWorkMessageConsumer extends AbstractSendSiteMessageConsumer{

    public static final String DEFAULT_QUEUE_NAME = "cmis.workflow.send.work.message";

    private final ApplicationConfig applicationConfig;

    public SendWorkMessageConsumer(ApplyService applyService,
                                   MessageServiceFeignClient messageServiceFeignClient,
                                   ApplicationConfig applicationConfig) {
        super(applyService, messageServiceFeignClient);
        this.applicationConfig = applicationConfig;
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
        super.sendMessage(id, channel, tag);
    }

    @Override
    protected Map<String, Object> createMessage(ApplyEntity apply) {

        List<ApplyApprovalEntity> list = getApplyService()
                .getApplyApprovalService()
                .lambdaQuery()
                .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                .list();

        List<TypeIdNameMeta> users = list.stream().map(TypeIdNameMeta::ofUserDetails).collect(Collectors.toList());

        String title = MessageFormat.format(applicationConfig.getPendingWorkTitle(), apply.getFormName());
        String content = MessageFormat.format(applicationConfig.getPendingWorkContent(), apply.getUsername(), apply.getCreationTime(), apply.getFormName());

        Map<String, Object> meta = createMetaData(apply);

        return MessageServiceFeignClient.createPushableNoticeSiteMessage(users, title, content, meta);
    }

}
