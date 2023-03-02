package com.github.dactiv.saas.message.service.basic;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.saas.commons.domain.meta.AttachmentMeta;
import com.github.dactiv.saas.commons.feign.AuthenticationServiceFeignClient;
import com.github.dactiv.saas.message.config.AttachmentConfig;
import com.github.dactiv.saas.message.domain.AttachmentMessage;
import com.github.dactiv.saas.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.saas.message.domain.entity.BatchMessageEntity;
import com.github.dactiv.saas.message.enumerate.AttachmentTypeEnum;
import com.github.dactiv.saas.message.service.BatchMessageService;
import com.rabbitmq.client.Channel;
import io.minio.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.*;

/**
 * 批量消息发送的抽象实现，用于对需要创建 tb_batch_message 记录的消息做一个统一处理
 *
 * @param <T> 批量消息数据的泛型实体类型
 * @param <S> 请求的消息数据泛型实体类型
 */
@Slf4j
public abstract class BatchMessageSender<T extends BasicMessageEntity, S extends BatchMessageEntity.Body> extends AbstractMessageSender<T> {

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    public static final String DEFAULT_ALL_USER_KEY = "ALL_USER";

    private BatchMessageService batchMessageService;

    /**
     * 文件管理服务
     */
    protected MinioTemplate minioTemplate;

    /**
     * 附件配置信息
     */
    protected AttachmentConfig attachmentConfig;

    /**
     * 会员用户服务
     */
    protected AuthenticationServiceFeignClient authenticationServiceFeignClient;

    /**
     * 线程池，用于批量发送消息时候异步使用。
     */
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * rabbit 配置
     */
    private RabbitProperties rabbitProperties;

    protected final Class<S> sendEntityClass;

    public BatchMessageSender() {
        this.sendEntityClass = ReflectionUtils.getGenericClass(this, 1);
    }

    @Override
    public RestResult<Object> sendMessage(List<T> result) {

        List<S> content = getBatchMessageBodyContent(result);

        Objects.requireNonNull(content, "批量消息 body 内容不能为空");

        RestResult<Object> restResult = RestResult.ofException(
                String.valueOf(HttpStatus.NO_CONTENT.value()),
                new SystemException("未知执行结果")
        );

        content
                .stream()
                .filter(c -> Retryable.class.isAssignableFrom(c.getClass()))
                .map(c -> Casts.cast(c, Retryable.class))
                .forEach(c -> c.setMaxRetryCount(getMaxRetryCount()));

        if (content.size() > 1) {

            BatchMessageEntity batchMessage = new BatchMessageEntity();

            batchMessage.setCount(content.size());

            AttachmentTypeEnum attachmentType = AttachmentTypeEnum.valueOf(entityClass);
            batchMessage.setType(attachmentType);

            batchMessageService.save(batchMessage);

            content.forEach(r -> r.setBatchId(batchMessage.getId()));

            Map<String, Object> data = Map.of(
                    DEFAULT_BATCH_MESSAGE_ID_KEY, batchMessage.getId(),
                    DEFAULT_MESSAGE_COUNT_KEY, content.size()
            );

            threadPoolTaskExecutor.execute(() -> {
                batchMessageCreated(batchMessage, result, content);
                boolean send = preSend(content);

                if (send) {
                    send(content);
                }

            });

            restResult = RestResult.ofSuccess(
                    "发送" + content.size() + "条 [" + getMessageType() + "] 消息成功",
                    data
            );
        } else {
            boolean send = preSend(content);

            if (send) {
                restResult = send(content);
            }
        }

        return restResult;
    }

    /**
     * 发送消息前的处理
     *
     * @param content 批量消息内容
     * @return true 继续发送，否则 false
     */
    protected boolean preSend(List<S> content) {
        return true;
    }

    /**
     * 发送批量消息
     *
     * @param result 批量消息内容
     * @return rest 结果集
     */
    protected abstract RestResult<Object> send(List<S> result);

    /**
     * 获取批量消息数据内容
     *
     * @param result 消息的请求数据泛型实体集合
     * @return 批量消息数据的泛型实体集合
     */
    protected abstract List<S> getBatchMessageBodyContent(List<T> result);

    /**
     * 更新批量消息
     *
     * @param body 批量消息接口实现类
     */
    public void updateBatchMessage(BatchMessageEntity.Body body) {

        BatchMessageEntity batchMessage = batchMessageService.get(body.getBatchId());

        if (ExecuteStatus.Success.equals(body.getExecuteStatus())) {
            batchMessage.setSuccessNumber(batchMessage.getSuccessNumber() - 1);
        } else if (ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
            batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
        }

        if (batchMessage.getCount().equals(batchMessage.getSuccessNumber() + batchMessage.getFailNumber())) {
            batchMessage.setExecuteStatus(ExecuteStatus.Success);
            batchMessage.setCompleteTime(new Date());

            onBatchMessageComplete(batchMessage);
        }

        batchMessageService.save(batchMessage);

    }

    /**
     * 当批量信息创建完成后，触发此方法
     *
     * @param batchMessage 批量信息实体
     * @param bodyResult   request 传过来的 body 参数集合
     */
    protected void batchMessageCreated(BatchMessageEntity batchMessage, List<T> bodyResult, List<S> content) {
        Map<String, byte[]> map = attachmentCache.computeIfAbsent(batchMessage.getId(), k -> new LinkedHashMap<>());

        List<AttachmentMeta> attachments = bodyResult
                .stream()
                .filter(t -> AttachmentMessage.class.isAssignableFrom(t.getClass()))
                .map(t -> Casts.cast(t, AttachmentMessage.class))
                .flatMap(t -> t.getAttachmentList().stream())
                .filter(a -> !map.containsKey(a.getName()))
                .toList();

        for (AttachmentMeta a : attachments) {
            FileObject fileObject = FileObject.of(attachmentConfig.getBucketName(getMessageType()), a.getName());
            try {
                GetObjectResponse response = minioTemplate.getObject(fileObject);
                map.put(a.getName(), IOUtils.toByteArray(response));
            } catch (Exception e) {
                log.error("读取 [" + a.getName() + "] 附件信息出现", e);
            }
        }
    }

    /**
     * 当批量信息发送完成时，触发此方法。
     *
     * @param batchMessage 批量信息实体
     */
    protected void onBatchMessageComplete(BatchMessageEntity batchMessage) {
        attachmentCache.remove(batchMessage.getId());
    }

    protected void sendMessage(Integer id,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        S entity = sendMessage(id);

        if (Objects.isNull(entity)) {
            channel.basicNack(tag, false, false);
            return;
        }

        if (entity instanceof ExecuteStatus.Body body) {

            if (ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
                throw new SystemException(body.getException());
            }

            if (entity instanceof Retryable retry && ExecuteStatus.Retrying.equals(body.getExecuteStatus()) && retry.getRetryCount() < getMaxRetryCount()) {
                throw new SystemException(body.getException());
            }
        }

        channel.basicAck(tag, false);
    }

    public abstract S sendMessage(Integer id);

    /**
     * 获取最大重试次数
     *
     * @return 重试次数
     */
    protected int getMaxRetryCount() {
        return rabbitProperties.getListener().getSimple().getRetry().isEnabled() ?
                rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts() :
                0;
    }

    @Autowired
    public void setBatchMessageService(BatchMessageService batchMessageService) {
        this.batchMessageService = batchMessageService;
    }

    @Autowired
    public void setMinioTemplate(MinioTemplate minioTemplate) {
        this.minioTemplate = minioTemplate;
    }

    @Autowired
    public void setAttachmentConfig(AttachmentConfig attachmentConfig) {
        this.attachmentConfig = attachmentConfig;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationServiceFeignClient authenticationServiceFeignClient) {
        this.authenticationServiceFeignClient = authenticationServiceFeignClient;
    }

    @Autowired
    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    @Autowired
    public void setRabbitProperties(RabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }
}
