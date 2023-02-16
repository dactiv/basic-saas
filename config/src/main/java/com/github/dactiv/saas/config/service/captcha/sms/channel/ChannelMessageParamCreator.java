package com.github.dactiv.saas.config.service.captcha.sms.channel;

import com.github.dactiv.saas.config.domain.meta.captcha.SmsMeta;

import java.util.Map;

/**
 * 短信渠道消息参数构造者接口
 *
 * @author maurice.chen
 */
public interface ChannelMessageParamCreator {

    /**
     * 创建渠道需要的参数信息
     *
     * @param entity  短信元数据
     * @param entry   字典内容
     * @param captcha 验证码
     * @return 发送的参数内容
     */
    Map<String, Object> createSendMessageParam(SmsMeta entity, Map<String, Object> entry, String captcha);

    /**
     * 获取渠道类型
     *
     * @return 渠道类型
     */
    String getType();
}
