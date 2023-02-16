package com.github.dactiv.saas.message.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.SystemConstants;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 消息发送者
 *
 * @author maurice
 */
public interface MessageSender {

    /**
     * 发送消息
     *
     * @param request http servlet request
     * @return rest 结果集
     * @throws Exception 发送错误时抛出
     */
    RestResult<Object> send(Map<String, Object> request) throws Exception;

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getMessageType();

    /**
     * 发送消息队列
     *
     * @param amqpTemplate 消息模版
     * @param messageMap   消息 map
     * @param id           消息 id
     */
    static void sendAmqpMessage(AmqpTemplate amqpTemplate, Map<String, Object> messageMap, Integer id) {

        Object queueName = messageMap.get(SystemConstants.NOTICE_MESSAGE_QUEUE_NAME);
        if (Objects.isNull(queueName)) {
            return;
        }

        Object body = messageMap.get(SystemConstants.NOTICE_MESSAGE_BODY_NAME);
        if (Objects.isNull(body)) {
            return;
        }

        Object exchangeName = messageMap.get(SystemConstants.NOTICE_MESSAGE_EXCHANGE_NAME);

        if (Objects.nonNull(exchangeName)) {
            amqpTemplate.convertAndSend(
                    exchangeName.toString(),
                    queueName.toString(),
                    body,
                    message -> {
                        String idValue = queueName + Casts.DEFAULT_DOT_SYMBOL + id;
                        message.getMessageProperties().setMessageId(idValue);
                        message.getMessageProperties().setCorrelationId(id.toString());
                        return message;
                    }
            );
        } else {
            amqpTemplate.convertAndSend(
                    queueName.toString(),
                    body,
                    message -> {
                        String idValue = queueName + Casts.DEFAULT_DOT_SYMBOL + id;
                        message.getMessageProperties().setMessageId(idValue);
                        message.getMessageProperties().setCorrelationId(id.toString());
                        return message;
                    }
            );
        }
    }
}
