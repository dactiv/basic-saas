package com.github.dactiv.saas.message.service.support.site;

import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import com.github.dactiv.saas.commons.domain.meta.wechat.TemplateMessageMeta;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;

/**
 * 微信消息模版发送者
 *
 * @author maurice.chen
 */
public interface WechatMessageTemplateSender {

    /**
     * 是否支持模版消息 id
     *
     * @param messageTemplateId 模版消息 id
     * @return true 是，否则 false
     */
    boolean isSupport(String messageTemplateId);

    /**
     * 创建消息模版
     *
     * @param entity            站内信实体
     * @param wechatUserDetails 微信用户明细
     * @return 消息模版元数据信息
     */
    TemplateMessageMeta createTemplateMessageMeta(SiteMessageEntity entity, WechatUserDetails wechatUserDetails);

    /**
     * 发送消息
     *
     * @param message 消息模版元数据信息
     * @throws Exception 发送错误时抛出
     */
    void sendMessage(TemplateMessageMeta message) throws Exception;

    /**
     * 获取消息模版名称
     *
     * @return 消息模版名称
     */
    String getName();
}
