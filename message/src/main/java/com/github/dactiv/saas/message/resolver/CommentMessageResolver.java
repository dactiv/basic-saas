package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;

/**
 * 评论消息解析器
 *
 * @author maurice.chen
 */
public interface CommentMessageResolver extends PreMessageResolver<CommentMessageEntity>, PostMessageResolver<CommentMessageEntity>{

    /**
     * 是否支持此类型
     *
     * @param type 评论消息类型
     * @return true 是，否则 false
     */
    boolean isSupport(String type);
}
