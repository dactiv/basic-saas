package com.github.dactiv.saas.config.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.config.captcha")
public class CaptchaConfig {

    /**
     * 验证码 token 缓存配置
     */
    private CacheProperties buildTokenCache = CacheProperties.of(
            "dactiv:saas:captcha:build:token:",
            TimeProperties.of(5, TimeUnit.MINUTES)
    );

    private CacheProperties interceptorTokenCache = CacheProperties.of(
            "dactiv:saas:captcha:interceptor:token:",
            TimeProperties.of(5, TimeUnit.MINUTES)
    );

    /**
     * 验证绑定 token 的参数后缀名
     */
    private String tokenParamNameSuffix = "captchaToken";

    /**
     * 验证码重试时间
     */
    private TimeProperties captchaRetryTime = TimeProperties.of(60, TimeUnit.SECONDS);

}
