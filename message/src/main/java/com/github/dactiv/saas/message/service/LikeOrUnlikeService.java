package com.github.dactiv.saas.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.message.dao.LikeOrUnlikeDao;
import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;
import com.github.dactiv.saas.message.resolver.LikeOrUnlikeResolver;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_like_or_unlike 的业务逻辑
 *
 * <p>Table: tb_like_or_unlike - 点赞或非点赞记录</p>
 *
 * @author maurice.chen
 * @see LikeOrUnlikeEntity
 * @since 2022-09-08 04:14:58
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LikeOrUnlikeService extends BasicService<LikeOrUnlikeDao, LikeOrUnlikeEntity> {

    private final List<LikeOrUnlikeResolver> likeOrUnlikeResolver;

    private final AmqpTemplate amqpTemplate;

    public LikeOrUnlikeService(ObjectProvider<LikeOrUnlikeResolver> likeOrUnlikeResolver,
                               AmqpTemplate amqpTemplate) {
        this.likeOrUnlikeResolver = likeOrUnlikeResolver.orderedStream().collect(Collectors.toList());
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
        return super.deleteById(id);
    }

    @Override
    public int deleteByEntity(Collection<LikeOrUnlikeEntity> entities, boolean errorThrow) {
        int sum = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (sum != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities + "] 的 [点赞] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int delete(Wrapper<LikeOrUnlikeEntity> wrapper) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public int deleteByEntity(LikeOrUnlikeEntity entity) {

        List<LikeOrUnlikeResolver> resolvers = likeOrUnlikeResolver
                .stream()
                .filter(c -> c.isSupport(entity.getTargetType()))
                .filter(c -> c.preDelete(entity))
                .toList();

        int result = super.deleteByEntity(entity);

        resolvers.forEach(c -> c.postDelete(entity));

        return result;

    }

    @Override
    @Concurrent("dactiv:saas:resources:message:comment:save:[#entity.userId]-[#entity.targetId]-[#entity.targetType]")
    public int save(LikeOrUnlikeEntity entity) {

        LikeOrUnlikeEntity orm = lambdaQuery()
                .eq(BasicUserDetails::getUserId, entity.getUserId())
                .eq(BasicUserDetails::getUserType, entity.getUserType())
                .eq(LikeOrUnlikeEntity::getTargetId, entity.getTargetId())
                .eq(LikeOrUnlikeEntity::getIsLike, entity.getIsLike().getValue())
                .eq(LikeOrUnlikeEntity::getTargetType, entity.getTargetType().getValue())
                .one();

        if (Objects.nonNull(orm)) {
            return deleteByEntity(orm);
        }

        List<LikeOrUnlikeResolver> resolvers = likeOrUnlikeResolver
                .stream()
                .filter(c -> c.isSupport(entity.getTargetType()))
                .filter(c -> c.preSave(entity))
                .toList();

        int result = super.save(entity);

        MessageSender.postSaveAndSendAmqpMessage(amqpTemplate, new LinkedList<>(resolvers), entity);

        return result;
    }
}
