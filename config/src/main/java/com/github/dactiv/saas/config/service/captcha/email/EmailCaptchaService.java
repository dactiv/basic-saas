package com.github.dactiv.saas.config.service.captcha.email;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.feign.AdminServiceFeignClient;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.config.config.CaptchaConfig;
import com.github.dactiv.saas.config.config.EmailCaptchaConfig;
import com.github.dactiv.saas.config.domain.meta.captcha.EmailMeta;
import com.github.dactiv.saas.config.service.captcha.AbstractMessageCaptchaService;
import com.github.dactiv.saas.config.service.captcha.ReusableCaptcha;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 邮件验证码服务
 *
 * @author maurice
 */
@Component
public class EmailCaptchaService extends AbstractMessageCaptchaService<EmailMeta, ReusableCaptcha> {
    /**
     * 默认的验证码服务类型名称
     */
    private static final String DEFAULT_TYPE = "email";

    private final EmailCaptchaConfig properties;

    public EmailCaptchaService(RedissonClient redissonClient,
                               CaptchaConfig captchaConfig,
                               @Qualifier("mvcValidator") @Autowired(required = false) Validator validator,
                               AdminServiceFeignClient adminServiceFeignClient,
                               MessageServiceFeignClient messageServiceFeignClient,
                               EmailCaptchaConfig properties) {
        super(redissonClient, captchaConfig, validator, adminServiceFeignClient, messageServiceFeignClient);
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> createSendMessageParam(EmailMeta entity, Map<String, Object> entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.get("value").toString(),
                entry.get(IdNameMeta.NAME_FIELD_NAME),
                captcha,
                properties.getCaptchaExpireTime().getValue()
        );

        // 构造参数，提交给消息服务发送信息
        param.put(MessageServiceFeignClient.DEFAULT_TITLE_KEY, entry.get(IdNameMeta.NAME_FIELD_NAME));
        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, content);
        param.put("toEmails", Collections.singletonList(entity.getEmail()));
        param.put(MessageServiceFeignClient.Constants.TYPE_FIELD, entry.get(IdNameMeta.NAME_FIELD_NAME));

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, DEFAULT_TYPE);

        return param;
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(properties.getRandomNumericCount());
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return properties.getCaptchaExpireTime();
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return properties.getCaptchaParamName();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {

        Map<String, Object> generate = new LinkedHashMap<>();

        generate.put("emailParamName", properties.getEmailParamName());
        generate.put("typeParamName", properties.getTypeParamName());

        return generate;
    }
}
