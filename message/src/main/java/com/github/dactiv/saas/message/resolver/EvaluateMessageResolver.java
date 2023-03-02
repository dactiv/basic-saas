package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.saas.message.domain.entity.EvaluateMessageEntity;
import com.github.dactiv.saas.message.enumerate.EvaluateMessageTypeEnum;

/**
 * 评价消息解析器
 *
 * @author maurice.chen
 */
public interface EvaluateMessageResolver extends PreMessageResolver<EvaluateMessageEntity>, PostMessageResolver<EvaluateMessageEntity> {

    /**
     * 是否支持此类型
     *
     * @param type 消息类型
     * @return true 是，否则 false
     */
    boolean isSupport(EvaluateMessageTypeEnum type);



}
