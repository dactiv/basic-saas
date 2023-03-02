package com.github.dactiv.saas.config.service.captcha.sms;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.config.config.CaptchaConfig;
import com.github.dactiv.saas.config.config.SmsCaptchaConfig;
import com.github.dactiv.saas.config.domain.meta.captcha.SmsMeta;
import com.github.dactiv.saas.config.service.captcha.AbstractMessageCaptchaService;
import com.github.dactiv.saas.config.service.captcha.ReusableCaptcha;
import com.github.dactiv.saas.config.service.captcha.sms.channel.ChannelMessageParamCreator;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 短信验证码服务
 *
 * @author maurice
 */
@Component
public class SmsCaptchaService extends AbstractMessageCaptchaService<SmsMeta, ReusableCaptcha> {

    /**
     * 默认的验证码服务类型名称
     */
    public static final String DEFAULT_TYPE = "sms";

    private final SmsCaptchaConfig smsCaptchaConfig;

    private final List<ChannelMessageParamCreator> messageParamCreators;

    public SmsCaptchaService(RedissonClient redissonClient,
                             @Qualifier("mvcValidator") @Autowired(required = false) Validator validator,
                             ConfigServiceFeignClient configServiceFeignClient,
                             MessageServiceFeignClient messageServiceFeignClient,
                             CaptchaConfig captchaConfig,
                             ObjectProvider<ChannelMessageParamCreator> channelMessageParamCreators,
                             SmsCaptchaConfig smsCaptchaConfig) {
        super(redissonClient, captchaConfig, validator, configServiceFeignClient, messageServiceFeignClient);
        this.messageParamCreators = channelMessageParamCreators.orderedStream().collect(Collectors.toList());
        this.smsCaptchaConfig = smsCaptchaConfig;
    }

    @Override
    protected Map<String, Object> createSendMessageParam(SmsMeta entity, Map<String, Object> entry, String captcha) {

        Optional<ChannelMessageParamCreator> optional = messageParamCreators
                .stream()
                .filter(c -> c.getType().equals(entity.getChannel()))
                .findFirst();

        if (optional.isEmpty()) {
            throw new ServiceException("找不到类型为 [" + entity.getChannel() + "] 的短信渠道商");
        }

        return optional.get().createSendMessageParam(entity, entry, captcha);
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(smsCaptchaConfig.getRandomNumericCount());
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return smsCaptchaConfig.getCaptchaExpireTime();
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return smsCaptchaConfig.getCaptchaParamName();
    }

    @Override
    public String getUsernameParamName() {
        return smsCaptchaConfig.getPhoneNumberParamName();
    }

    @Override
    protected Map<String, Object> createPostArgs() {
        Map<String, Object> post = super.createPostArgs();
        post.put("phoneNumberParamName", getUsernameParamName());
        return post;
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {

        Map<String, Object> generate = new LinkedHashMap<>();

        generate.put("phoneNumberParamName", getUsernameParamName());
        generate.put("typeParamName", smsCaptchaConfig.getTypeParamName());

        return generate;
    }
}
