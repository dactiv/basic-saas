package com.github.dactiv.saas.authentication.consumer;

import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.saas.authentication.resolver.DepartmentResolver;
import com.github.dactiv.saas.authentication.service.DepartmentService;
import com.github.dactiv.saas.commons.SystemConstants;
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
import java.util.Objects;
import java.util.Optional;

/**
 * 统计部门人数消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class CountDepartmentPersonConsumer {

    public static final String DEFAULT_QUEUE_NAME = "dactiv.saas.authentication.count.department.person";

    private final DepartmentService departmentService;

    public CountDepartmentPersonConsumer(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    @Concurrent("dactiv:saas:authentication:department:count-person:[#id]")
    public void onMessage(@Payload Integer id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        DepartmentEntity entity = departmentService.get(id);

        if (Objects.isNull(entity)) {
            channel.basicNack(tag, false, false);
            return;
        }

        Optional<DepartmentResolver> optional = departmentService
                .getDepartmentResolvers()
                .stream()
                .filter(s -> s.isSupport(entity))
                .findFirst();

        if (optional.isEmpty()) {
            channel.basicNack(tag, false, false);
            return;
        }

        int count = optional.get().countPerson(entity);
        if (count < 0) {
            count = 0;
        }

        entity.setCount(count);
        departmentService.save(entity);

        channel.basicAck(tag, false);
    }
}
