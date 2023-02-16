package com.github.dactiv.saas.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.message.enumerate.EvaluateMessageTypeEnum;
import com.github.dactiv.saas.message.resolver.EvaluateMessageResolver;
import com.github.dactiv.saas.message.dao.EvaluateMessageDao;
import com.github.dactiv.saas.message.domain.body.evaluate.EvaluateAppendRequestBody;
import com.github.dactiv.saas.message.domain.entity.EvaluateMessageEntity;
import com.github.dactiv.saas.message.domain.meta.EvaluateMessageAppendMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_evaluate_message 的业务逻辑
 *
 * <p>Table: tb_evaluate_message - 评价消息</p>
 *
 * @author maurice.chen
 * @see EvaluateMessageEntity
 * @since 2022-06-30 06:08:37
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EvaluateMessageService extends BasicService<EvaluateMessageDao, EvaluateMessageEntity> {

    private final List<EvaluateMessageResolver> evaluateMessageResolvers;

    private final AmqpTemplate amqpTemplate;

    public EvaluateMessageService(ObjectProvider<EvaluateMessageResolver> evaluateMessageResolvers,
                                  AmqpTemplate amqpTemplate) {
        this.evaluateMessageResolvers = evaluateMessageResolvers.orderedStream().collect(Collectors.toList());
        this.amqpTemplate = amqpTemplate;
    }

    public Integer getId(Integer targetId, Integer targetType, Integer userId) {
        EvaluateMessageEntity entity = lambdaQuery()
                .select(EvaluateMessageEntity::getId)
                .eq(EvaluateMessageEntity::getTargetId, targetId)
                .eq(EvaluateMessageEntity::getTargetType, targetType)
                .eq(BasicUserDetails::getUserId, userId)
                .one();

        return Objects.nonNull(entity) ? entity.getId() : null;
    }

    @Override
    @Concurrent("dactiv:saas:message:evaluate:save:[#entity.userId]-[#entity.targetId]-[#entity.targetType.value]")
    public int save(EvaluateMessageEntity entity) {
        if (Objects.isNull(entity.getId()) && Objects.nonNull(getId(entity.getTargetId(), entity.getTargetType().getValue(), entity.getUserId()))) {
            throw new SystemException("ID 为 [" + entity.getUserId() + "] 的用户已对 ID 为 [" + entity.getTargetId() + "] 的 [" + entity.getTargetType().getName() + "] 项目做出评价，只能进行追加评价。");
        }

        List<EvaluateMessageResolver> resolvers = evaluateMessageResolvers
                .stream()
                .filter(r -> r.isSupport(entity.getTargetType()))
                .filter(c -> c.preSave(entity))
                .collect(Collectors.toList());

        int result = super.save(entity);

        List<Map<String, Object>> mapList = resolvers.stream().map(r -> r.postSave(entity)).collect(Collectors.toList());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mapList.forEach(map -> MessageSender.sendAmqpMessage(amqpTemplate, map, entity.getId()));
            }
        });

        return result;
    }

    @Concurrent(value = "dactiv:saas:message:evaluate:save-append:[#meta.evaluateId]", condition = "[#id] != null")
    public void saveAppend(EvaluateAppendRequestBody meta, SecurityUserDetails userDetails) {
        meta.setId(String.valueOf(System.currentTimeMillis()));
        EvaluateMessageEntity message = Objects.requireNonNull(get(meta.getEvaluateId()), "找不到 evaluateId 为 [" + meta.getEvaluateId() + "] 的评价记录");

        if (!userDetails.getId().equals(message.getUserId())) {
            throw new SystemException("ID 为 [" + message.getId() + "] 的评价不属于 ID 为 [" + userDetails.getId() + "] 的用户");
        }

        Optional<EvaluateMessageAppendMeta> exist = message
                .getAppend()
                .stream()
                .filter(a -> a.getId().equals(meta.getId()))
                .findFirst();

        if (exist.isEmpty()) {
            message.getAppend().add(Casts.of(meta, EvaluateMessageAppendMeta.class));
        } else {
            EvaluateMessageAppendMeta appendMeta = exist.get();
            appendMeta.setUpdateTime(new Date());
        }

        updateById(message);
    }

    public void deleteAppend(Integer id, String appendId, SecurityUserDetails userDetails) {
        Assert.notNull(id, "id 参数不能为 null");
        Assert.notNull(appendId, "appendId 参数不能为 null");
        EvaluateMessageEntity message = Objects.requireNonNull(get(id), "找不到 ID 为 [" + id + "] 的评价记录");
        if (!userDetails.getId().equals(message.getUserId())) {
            throw new SystemException("ID 为 [" + message.getId() + "] 的评价不属于 ID 为 [" + userDetails.getId() + "] 的用户");
        }

        message.getAppend().removeIf(e -> StringUtils.equals(appendId, e.getId()));

        updateById(message);
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteById(Collection<? extends Serializable> ids, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteById(Collection<? extends Serializable> ids, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int deleteById(Serializable id) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteById(Serializable id, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int deleteByEntity(Collection<EvaluateMessageEntity> entities) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteByEntity(EvaluateMessageEntity entity, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int deleteByEntity(Collection<EvaluateMessageEntity> entities, boolean errorThrow) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteByEntity(EvaluateMessageEntity entity, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int deleteByEntity(EvaluateMessageEntity entity) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteByEntity(EvaluateMessageEntity entity, SecurityUserDetails userDetails) 执行删除操作");
    }

    @Override
    public int delete(Wrapper<EvaluateMessageEntity> wrapper) {
        throw new UnsupportedOperationException("不支持此操作，请使用 deleteByEntity(EvaluateMessageEntity entity, SecurityUserDetails userDetails) 执行删除操作");
    }

    public int deleteById(Collection<? extends Serializable> ids, SecurityUserDetails userDetails) {
        return ids.stream().mapToInt(e -> deleteById(e, userDetails)).sum();
    }

    public int deleteById(Serializable id, SecurityUserDetails userDetails) {
        EvaluateMessageEntity entity = get(id);
        return deleteByEntity(entity, userDetails);
    }

    public int deleteByEntity(EvaluateMessageEntity entity, SecurityUserDetails userDetails) {

        if (ResourceSourceEnum.STUDENT_SOURCE_VALUE.equals(userDetails.getType()) && !userDetails.getId().equals(entity.getUserId())) {
            throw new SystemException("ID 为 [" + entity.getId() + "] 的评价不属于 ID 为 [" + userDetails.getId() + "] 的用户");
        }

        List<EvaluateMessageResolver> resolvers = evaluateMessageResolvers
                .stream()
                .filter(c -> c.isSupport(entity.getTargetType()))
                .filter(c -> c.preDelete(entity))
                .collect(Collectors.toList());

        int result = super.deleteByEntity(entity);

        resolvers
                .stream()
                .map(c -> c.postDelete(entity))
                .forEach(map -> MessageSender.sendAmqpMessage(amqpTemplate, map, entity.getId()));

        return result;
    }

    public DoubleSummaryStatistics summaryStatistics(Integer targetId, EvaluateMessageTypeEnum type) {
        return lambdaQuery()
                .select(EvaluateMessageEntity::getRate)
                .eq(EvaluateMessageEntity::getTargetType, type.getValue())
                .eq(EvaluateMessageEntity::getTargetId, targetId)
                .list()
                .stream()
                .mapToDouble(EvaluateMessageEntity::getRate)
                .summaryStatistics();
    }
}
