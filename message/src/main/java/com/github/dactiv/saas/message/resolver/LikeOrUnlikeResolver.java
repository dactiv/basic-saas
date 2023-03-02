package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;
import com.github.dactiv.saas.message.enumerate.LikeOrUnlikeTargetTypeEnum;

/**
 * 点赞或非点赞记录解析器
 *
 * @author maurice.chen
 */
public interface LikeOrUnlikeResolver extends PreMessageResolver<LikeOrUnlikeEntity>, PostMessageResolver<LikeOrUnlikeEntity> {

    /**
     * 是否支持此类型
     *
     * @param type 目标类型枚举
     * @return true 是，否则 false
     */
    boolean isSupport(LikeOrUnlikeTargetTypeEnum type);


}
