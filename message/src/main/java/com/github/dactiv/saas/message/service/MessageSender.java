package com.github.dactiv.saas.message.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.message.resolver.PostMessageResolver;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
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

    static <T extends BasicIdentification<Integer>>  void postSaveAndSendAmqpMessage(AmqpTemplate amqpTemplate, List<PostMessageResolver<T>> resolvers, T entity) {
        List<Map<String, Object>> mapList = resolvers.stream().map(r -> r.postSave(entity)).toList();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mapList.forEach(map -> MessageSender.sendAmqpMessage(amqpTemplate, map, entity.getId()));
            }
        });
    }

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
