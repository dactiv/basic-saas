package com.github.dactiv.saas.config.service.captcha.sms.channel.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.saas.config.domain.meta.captcha.SmsMeta;
import com.github.dactiv.saas.config.service.captcha.sms.SmsCaptchaService;
import com.github.dactiv.saas.config.service.captcha.sms.channel.ChannelMessageParamCreator;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 阿里云短信渠道消息参数构造者实现
 *
 * @author maurice.chen
 */
@Component
public class AliYunChannelMessageParamCreator implements ChannelMessageParamCreator {

    public static final String DEFAULT_TYPE = "AliYun";

    @Override
    public Map<String, Object> createSendMessageParam(SmsMeta entity, Map<String, Object> entry, String captcha) {
        Map<String, Object> param = new LinkedHashMap<>();

        //noinspection unchecked
        Map<String, Object> aliYunMeta = Casts.readValue(entry.get(ValueEnum.FIELD_NAME).toString(), Map.class);

        aliYunMeta.put("templateParam", "{\"code\":\"" + captcha + "\"}");
        // 构造参数，提交给消息服务发送信息
        param.put("aliYunMeta", aliYunMeta);

        param.put(MessageServiceFeignClient.Constants.Sms.PHONE_NUMBERS_FIELD, Collections.singletonList(entity.getPhoneNumber()));
        param.put(MessageServiceFeignClient.Constants.TYPE_FIELD, entity.getMessageType());
        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, SmsCaptchaService.DEFAULT_TYPE);

        return param;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }
}
