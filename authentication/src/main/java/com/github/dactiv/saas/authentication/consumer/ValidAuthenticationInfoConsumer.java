package com.github.dactiv.saas.authentication.consumer;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.saas.authentication.domain.meta.IpRegionMeta;
import com.github.dactiv.saas.authentication.security.ip.IpResolver;
import com.github.dactiv.saas.authentication.service.AuthenticationInfoService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 验证认证信息消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class ValidAuthenticationInfoConsumer {

    public static final String DEFAULT_QUEUE_NAME = "dactiv.saas.authentication.valid.info";

    private final AuthenticationInfoService authenticationInfoService;

    private final ApplicationConfig config;

    private final List<IpResolver> ipResolvers;

    public ValidAuthenticationInfoConsumer(AuthenticationInfoService authenticationInfoService,
                                           ApplicationConfig config,
                                           List<IpResolver> ipResolvers) {
        this.authenticationInfoService = authenticationInfoService;
        this.config = config;
        this.ipResolvers = ipResolvers;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void validAuthenticationInfo(@Payload AuthenticationInfoEntity info,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        //IpRegionMeta ipRegionMeta = info.getIpRegion();
        Optional<IpResolver> ipResolver = ipResolvers
                .stream()
                .filter(resolver -> resolver.isSupport(config.getIpResolverType()))
                .findFirst();


        if (ipResolver.isEmpty()) {
            log.warn("找不到 ip 解析器，不需要解析登陆 ip");
            channel.basicNack(tag, false, false);
            return;
        }


        Object ipAddress = info.getIpRegion().get(IpRegionMeta.IP_ADDRESS_NAME);
        if (Objects.isNull(ipAddress)) {
            log.warn("找不到 ip 信息，不需要解析登陆 ip");
            channel.basicNack(tag, false, false);
            return;
        }

        IpRegionMeta source = ipResolver.get().getIpRegionMeta(ipAddress.toString());
        //noinspection unchecked
        info.setIpRegion(Casts.convertValue(source, Map.class));

        authenticationInfoService.save(info);

        authenticationInfoService.updateById(info);
        authenticationInfoService.validAuthenticationInfo(info);

        channel.basicAck(tag, false);

    }
}
