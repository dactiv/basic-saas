package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.saas.message.enumerate.LikeOrUnlikeTargetTypeEnum;
import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;

import java.util.Map;

/**
 * 点赞或非点赞记录解析器
 *
 * @author maurice.chen
 */
public interface LikeOrUnlikeResolver {

    /**
     * 是否支持此类型
     *
     * @param type 目标类型枚举
     * @return true 是，否则 false
     */
    boolean isSupport(LikeOrUnlikeTargetTypeEnum type);

    /**
     * 保存之前触发此方法
     *
     * @param likeOrUnlike 点赞或非点赞记录
     * @return true 继续执行保存，否则 false
     */
    default boolean preSave(LikeOrUnlikeEntity likeOrUnlike) {
        return true;
    }

    /**
     * 保持之后触发此方法
     *
     * @param likeOrUnlike 点赞或非点赞记录
     */
    default Map<String, Object> postSave(LikeOrUnlikeEntity likeOrUnlike) {
        return Map.of();
    }

    /**
     * 删除之前触发此方法
     *
     * @param likeOrUnlike 点赞或非点赞记录
     * @return true 继续执行删除，否则 false
     */
    default boolean preDelete(LikeOrUnlikeEntity likeOrUnlike) {
        return true;
    }

    /**
     * 删除之后触发此方法
     *
     * @param likeOrUnlike 点赞或非点赞记录
     */
    default Map<String, Object> postDelete(LikeOrUnlikeEntity likeOrUnlike) {
        return Map.of();
    }
}
