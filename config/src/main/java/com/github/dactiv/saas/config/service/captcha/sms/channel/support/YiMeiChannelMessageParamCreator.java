package com.github.dactiv.saas.config.service.captcha.sms.channel.support;

import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.saas.config.config.SmsCaptchaConfig;
import com.github.dactiv.saas.config.domain.meta.captcha.SmsMeta;
import com.github.dactiv.saas.config.service.captcha.sms.SmsCaptchaService;
import com.github.dactiv.saas.config.service.captcha.sms.channel.ChannelMessageParamCreator;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.enumeration.MessageTypeEnum;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 亿美短信渠道消息参数构造者实现
 *
 * @author maurice.chen
 */
@Component
public class YiMeiChannelMessageParamCreator implements ChannelMessageParamCreator {

    public static final String DEFAULT_TYPE = "YiMei";

    private final SmsCaptchaConfig smsCaptchaConfig;

    public YiMeiChannelMessageParamCreator(SmsCaptchaConfig smsCaptchaConfig) {
        this.smsCaptchaConfig = smsCaptchaConfig;
    }

    @Override
    public Map<String, Object> createSendMessageParam(SmsMeta entity, Map<String, Object> entry, String captcha) {
        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.get(ValueEnum.FIELD_NAME).toString(),
                entry.get(IdNameMeta.NAME_FIELD_NAME),
                captcha,
                smsCaptchaConfig.getCaptchaExpireTime().getValue()
        );

        // 构造参数，提交给消息服务发送信息
        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, content);
        param.put(MessageServiceFeignClient.Constants.Sms.PHONE_NUMBERS_FIELD, Collections.singletonList(entity.getPhoneNumber()));
        param.put(MessageServiceFeignClient.Constants.TYPE_FIELD, MessageTypeEnum.SYSTEM.toString());

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, SmsCaptchaService.DEFAULT_TYPE);

        return param;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }
}
