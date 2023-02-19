package com.github.dactiv.saas.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.message.dao.CommentMessageDao;
import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;
import com.github.dactiv.saas.message.resolver.CommentMessageResolver;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_comment_message 的业务逻辑
 *
 * <p>Table: tb_comment_message - 评论消息</p>
 *
 * @author maurice.chen
 * @see CommentMessageEntity
 * @since 2022-07-01 10:59:09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CommentMessageService extends BasicService<CommentMessageDao, CommentMessageEntity> {

    private final List<CommentMessageResolver> commentMessageResolvers;

    private final AmqpTemplate amqpTemplate;

    public CommentMessageService(ObjectProvider<CommentMessageResolver> commentMessageResolvers,
                                 AmqpTemplate amqpTemplate) {
        this.commentMessageResolvers = commentMessageResolvers.orderedStream().collect(Collectors.toList());
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int sum = get(ids).stream().mapToInt(this::deleteByEntity).sum();
        if (sum != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int deleteById(Serializable id) {
        return deleteByEntity(get(id));
    }

    @Override
    public int deleteByEntity(Collection<CommentMessageEntity> entities, boolean errorThrow) {
        int sum = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (sum != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int delete(Wrapper<CommentMessageEntity> wrapper) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public int deleteByEntity(CommentMessageEntity entity) {

        if (Objects.isNull(entity)) {
            return 0;
        }

        CommentMessageEntity comment = get(entity.getParentId());
        if (Objects.nonNull(comment)) {
            comment.setReplyCount(comment.getReplyCount() - 1);
            updateById(comment);
        }

        lambdaQuery().eq(CommentMessageEntity::getParentId, entity.getId()).list().forEach(this::deleteByEntity);

        List<CommentMessageResolver> resolvers = commentMessageResolvers
                .stream()
                .filter(c -> c.isSupport(entity.getTargetType()))
                .filter(c -> c.preDelete(entity))
                .toList();

        int result = super.deleteByEntity(entity);

        resolvers
                .stream()
                .map(c -> c.postDelete(entity))
                .forEach(map -> MessageSender.sendAmqpMessage(amqpTemplate, map, entity.getId()));

        return result;

    }

    @Override
    @Concurrent("dactiv:saas:resources:message:comment:save:[#entity.userId]-[#entity.targetId]-[#entity.targetType]")
    public int save(CommentMessageEntity entity) {

        if (Objects.nonNull(entity.getParentId())) {
            CommentMessageEntity parent = Objects.requireNonNull(
                    get(entity.getParentId()),
                    "找不到父为 [" + entity.getParentId() + "] 的评论记录"
            );
            parent.setReplyCount(parent.getReplyCount() + 1);
            updateById(parent);
        }

        List<CommentMessageResolver> resolvers = commentMessageResolvers
                .stream()
                .filter(c -> c.isSupport(entity.getTargetType()))
                .filter(c -> c.preSave(entity))
                .toList();

        int result = super.save(entity);

        List<Map<String, Object>> mapList = resolvers.stream().map(r -> r.postSave(entity)).toList();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mapList.forEach(map -> MessageSender.sendAmqpMessage(amqpTemplate, map, entity.getId()));
            }
        });

        return result;
    }

    public void loadChildren(CommentMessageEntity entity) {
        if (entity.getReplyCount() <= 0) {
            return;
        }
        List<CommentMessageEntity> commentMessageList = lambdaQuery()
                .eq(CommentMessageEntity::getParentId, entity.getId())
                .list();
        entity.setChildren(new LinkedList<>(commentMessageList));
    }
}
