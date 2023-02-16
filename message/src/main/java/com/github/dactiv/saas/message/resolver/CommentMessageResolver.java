package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;

import java.util.Map;

/**
 * 评论消息解析器
 *
 * @author maurice.chen
 */
public interface CommentMessageResolver {

    /**
     * 是否支持此类型
     *
     * @param type 评论消息类型
     * @return true 是，否则 false
     */
    boolean isSupport(String type);

    /**
     * 保存前调用此方法
     *
     * @param entity 实体内容
     * @return true 继续执行保存，否则 false
     */
    default boolean preSave(CommentMessageEntity entity) {
        return true;
    }

    /**
     * 保存后，调用此方法
     *
     * @param entity 实体内容
     */
    default Map<String, Object> postSave(CommentMessageEntity entity) {
        return null;
    }

    /**
     * 删除前，调用此方法
     *
     * @param entity 实体内容
     * @return true 继续执行删除，否则 false
     */
    default boolean preDelete(CommentMessageEntity entity) {
        return true;
    }

    /**
     * 删除后，调用此方法
     *
     * @param entity 实体内容
     */
    default Map<String, Object> postDelete(CommentMessageEntity entity) {
        return Map.of();
    }
}
