package com.github.dactiv.saas.workflow.consumer;


import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.workflow.config.ApplicationConfig;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import com.github.dactiv.saas.workflow.enumerate.ApplyCopyStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkTypeEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * 完成流程申请消息消费者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class SendCompleteApplyMessageConsumer extends AbstractSendSiteMessageConsumer{

    public static final String DEFAULT_QUEUE_NAME = "cmis.workflow.send.complete.apply.message";

    private final ApplicationConfig applicationConfig;

    public SendCompleteApplyMessageConsumer(ApplyService applyService,
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

        ApplyEntity apply = getApplyService().get(id);

        if (Objects.isNull(apply)){
            log.warn("找不到 ID 为 [" + id + "] 的审批申请信息");
            channel.basicNack(tag, false, false);
            return ;
        }

        if (!ApplyStatusEnum.SCHEDULE_STATUS.contains(apply.getStatus())) {
            log.warn("ID 为 [" + id + "] 的审批申请状态不正确，状态应该为:" + ApplyStatusEnum.SCHEDULE_STATUS + ", 但该状态为:" + apply.getStatus());
            channel.basicNack(tag, false, false);
            return ;
        }

        if (ApplyStatusEnum.AGREE.equals(apply.getStatus())) {
            List<ApplyCopyEntity> applyCopyList = getApplyService()
                    .getApplyCopyService()
                    .lambdaQuery()
                    .eq(ApplyCopyEntity::getApplyId, apply.getId())
                    .list();

            applyCopyList
                    .stream()
                    .peek(a -> a.setCopyTime(new Date()))
                    .peek(a -> a.setStatus(ApplyCopyStatusEnum.PROCESSED))
                    .peek(a -> getApplyService().getApplyCopyService().updateById(a))
                    .map(a -> this.createCopyMeWork(apply, a))
                    .forEach(w -> getApplyService().getWorkService().insert(w));
        }

        sendNoticeSiteMessage(apply);
        ExecuteStatus.success(apply);
        getApplyService().updateById(apply);

        channel.basicAck(tag, false);
    }

    private void sendNoticeSiteMessage(ApplyEntity apply) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_SITE_TYPE_VALUE);

        Map<String, Object> messages = this.createMessage(apply);
        param.put(MessageServiceFeignClient.DEFAULT_MESSAGES_KEY, List.of(messages));

        try {
            RestResult<Object> result = this.getMessageServiceFeignClient().send(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }
        } catch (Exception e) {
            log.error("发送站内信失败", e);
        }
    }

    private WorkEntity createCopyMeWork(ApplyEntity entity, ApplyCopyEntity applyCopyEntity) {
        WorkEntity work = WorkEntity.of(entity, WorkTypeEnum.COPY);

        work.setUsername(applyCopyEntity.getUsername());
        work.setUserId(applyCopyEntity.getUserId());
        work.setUserType(applyCopyEntity.getUserType());
        work.setStatus(WorkStatusEnum.PROCESSED);

        return work;
    }

    @Override
    protected Map<String, Object> createMessage(ApplyEntity apply) {
        Map<String, Object> meta = createMetaData(apply);

        return MessageServiceFeignClient.createPushableNoticeSiteMessage(
                List.of(TypeIdNameMeta.ofUserDetails(apply)),
                MessageFormat.format(applicationConfig.getCompleteApplyTitle(), apply.getFormName(), apply.getStatus().getName()),
                MessageFormat.format(applicationConfig.getCompleteApplyContent(),  apply.getFormName(), apply.getCreationTime()),
                meta
        );
    }
}
