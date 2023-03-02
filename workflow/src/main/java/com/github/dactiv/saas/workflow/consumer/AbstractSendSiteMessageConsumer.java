package com.github.dactiv.saas.workflow.consumer;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.MessageLinkTypeEnum;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public abstract class AbstractSendSiteMessageConsumer {

    @Getter
    private final ApplyService applyService;
    @Getter
    private final MessageServiceFeignClient messageServiceFeignClient;

    public AbstractSendSiteMessageConsumer(ApplyService applyService,
                                           MessageServiceFeignClient messageServiceFeignClient) {
        this.applyService = applyService;
        this.messageServiceFeignClient = messageServiceFeignClient;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(@Payload Integer id,
                            Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        ApplyEntity apply = applyService.get(id);
        
        if (Objects.isNull(apply) || !ApplyStatusEnum.SCHEDULE_STATUS.contains(apply.getStatus())) {
            channel.basicNack(tag, false, false);
            return ;
        }

        Map<String, Object> param = new LinkedHashMap<>();
        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_SITE_TYPE_VALUE);

        Map<String, Object> messages = this.createMessage(apply);
        param.put(MessageServiceFeignClient.DEFAULT_MESSAGES_KEY, messages);

        try {
            RestResult<Object> result = messageServiceFeignClient.send(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }
        } catch (Exception e) {
            log.error("发送站内信失败", e);
        }

        ExecuteStatus.success(apply);
        applyService.updateById(apply);

        channel.basicAck(tag, false);
    }

    protected abstract Map<String, Object> createMessage(ApplyEntity apply);

    protected Map<String,Object> createMetaData(ApplyEntity entity) {
        Map<String, Object> link = Map.of(
                MessageServiceFeignClient.Constants.TYPE_FIELD, MessageLinkTypeEnum.WORKFLOW_APPLY.getValue(),
                IdEntity.ID_FIELD_NAME, entity.getId()
        );

        return Map.of(MessageServiceFeignClient.Constants.Site.LINKE_META_FIELD, link);
    }
}
