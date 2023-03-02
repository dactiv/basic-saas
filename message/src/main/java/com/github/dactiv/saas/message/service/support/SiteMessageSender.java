package com.github.dactiv.saas.message.service.support;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.idempotent.ConcurrentConfig;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.message.config.site.SiteConfig;
import com.github.dactiv.saas.message.domain.AttachmentMessage;
import com.github.dactiv.saas.message.domain.body.site.SiteMessageBody;
import com.github.dactiv.saas.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.saas.message.service.SiteMessageService;
import com.github.dactiv.saas.message.service.basic.BatchMessageSender;
import com.github.dactiv.saas.message.service.support.site.SiteMessageChannelSender;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Slf4j
@Component
public class SiteMessageSender extends BatchMessageSender<SiteMessageBody, SiteMessageEntity> implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "dactiv.saas.message.site.queue";

    /**
     * 默认的消息类型
     */
    public static final String DEFAULT_TYPE = "site";

    public static final String ALL_USER_TYPE = "ALL";

    private final SiteMessageService siteMessageService;

    private final List<SiteMessageChannelSender> siteMessageChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final SiteConfig config;

    public SiteMessageSender(SiteMessageService siteMessageService,
                             List<SiteMessageChannelSender> siteMessageChannelSenderList,
                             AmqpTemplate amqpTemplate,
                             ConcurrentInterceptor concurrentInterceptor,
                             SiteConfig config) {

        this.siteMessageService = siteMessageService;
        this.siteMessageChannelSenderList = siteMessageChannelSenderList;
        this.amqpTemplate = amqpTemplate;
        this.concurrentInterceptor = concurrentInterceptor;
        this.config = config;
    }

    /**
     * 发送站内信
     *
     * @param id      站内信实体 id
     * @param channel 频道信息
     * @param tag     ack 值
     * @throws IOException 发送失败或确认 ack 错误时抛出。
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void sendMessage(@Payload Integer id,
                            Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        super.sendMessage(id, channel, tag);
    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SiteMessageEntity sendMessage(Integer id) {

        SiteMessageEntity entity = siteMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        List<SiteMessageChannelSender> siteMessageChannelSenders = getSiteMessageChannelSender(config.getChannel());

        entity.setLastSendTime(new Date());
        entity.setChannel(config.getChannel());
        entity.setRetryCount(entity.getRetryCount() + 1);

        try {
            Map<String, RestResult<Map<String, Object>>> restResults = new LinkedHashMap<>();
            for (SiteMessageChannelSender sender : siteMessageChannelSenders) {
                RestResult<Map<String, Object>> result = sender.sendSiteMessage(entity);
                restResults.put(sender.getType(), result);
            }

            if (restResults.values().stream().allMatch(r -> config.getSuccessStatus().contains(r.getStatus()))) {
                ExecuteStatus.success(entity);
            } else {
                List<String> messages = restResults
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getStatus() != HttpStatus.OK.value())
                        .map(e -> e.getKey() + CacheProperties.DEFAULT_SEPARATOR + e.getValue().getMessage())
                        .toList();
                ExecuteStatus.failure(entity, StringUtils.join(messages, SpringMvcUtils.COMMA_STRING));
            }

        } catch (Exception e) {
            log.error("发送站内信失败", e);
            ExecuteStatus.failure(entity, e.getMessage());
        }

        siteMessageService.save(entity);

        if (Objects.nonNull(entity.getBatchId())) {
            ConcurrentConfig properties = config.getBatchUpdateConcurrent().ofSuffix(entity.getBatchId());
            concurrentInterceptor.invoke(properties, () -> updateBatchMessage(entity));
        }

        return entity;
    }

    /**
     * 获取站内信消息渠道发送者
     *
     * @param channel 渠道类型
     * @return 站内信消息渠道发送者
     */
    private List<SiteMessageChannelSender> getSiteMessageChannelSender(List<String> channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.contains(s.getType()))
                .collect(Collectors.toList());
    }

    /**
     * 通过站内信消息 body 构造站内信消息并保存信息
     *
     * @param body 站内信消息 body
     * @return 邮件消息流
     */
    private Stream<SiteMessageEntity> createSiteMessage(SiteMessageBody body) {

        if (CollectionUtils.isNotEmpty(body.getToUsers())) {

            TypeIdNameMeta typeIdNameMeta = body.getToUsers().iterator().next();

            if (typeIdNameMeta.getType().equals(ALL_USER_TYPE)) {

                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("filter_[status_eq]", DisabledOrEnabled.Enabled.getValue());

                List<Map<String, Object>> teachers = authenticationServiceFeignClient.findMemberUser(filter);
                List<TypeIdNameMeta> users = convertTypeIdNameMeta(teachers, ResourceSourceEnum.MEMBER.toString());

                body.setToUsers(users);
            }

        }

        List<SiteMessageEntity> result = new LinkedList<>();

        for (TypeIdNameMeta meta : body.getToUsers()) {

            SiteMessageEntity entity = ofEntity(body);

            entity.setUserId(meta.getId());
            entity.setUsername(meta.getName());
            entity.setUserType(meta.getType());

            result.add(entity);
        }

        return result.stream();
    }

    private List<TypeIdNameMeta> convertTypeIdNameMeta(List<Map<String, Object>> users, String userType) {
        List<TypeIdNameMeta> result = new LinkedList<>();

        for (Map<String, Object> teacher : users) {
            Integer id = Casts.cast(teacher.get(IdEntity.ID_FIELD_NAME), Integer.class);
            String name = SecurityUserDetailsConstants.getRealName(teacher);
            result.add(TypeIdNameMeta.of(id, name, userType));
        }

        return result;
    }

    /**
     * 创建站内信消息实体
     *
     * @param body 站内信消息 body
     * @return 站内信消息实体
     */
    private SiteMessageEntity ofEntity(SiteMessageBody body) {
        SiteMessageEntity entity = Casts.of(body, SiteMessageEntity.class, AttachmentMessage.ATTACHMENT_LIST_FIELD_NAME);

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setAttachmentList(body.getAttachmentList());
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<SiteMessageEntity> content) {
        siteMessageService.save(content);
        return true;
    }

    @Override
    protected RestResult<Object> send(List<SiteMessageEntity> entities) {
        entities
                .stream()
                .map(BasicMessageEntity::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条站内信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SiteMessageEntity> getBatchMessageBodyContent(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessage).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(getMessageType())));
    }
}
