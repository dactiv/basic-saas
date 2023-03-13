package com.github.dactiv.saas.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.authentication.app")
public class ApplicationConfig {

    public static final String DEFAULT_LOGOUT_URL = "/logout";

    public static final List<String> DEFAULT_CAPTCHA_AUTHENTICATION_TYPES = Arrays.asList(
            ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
            ResourceSourceEnum.MEMBER_SOURCE_VALUE,
            DefaultUserDetailsService.DEFAULT_TYPES
    );

    /**
     * 管理员组 id
     */
    private Integer adminGroupId = 1;

    /**
     * 允许认证错误次数，当达到峰值时，出现验证码
     */
    private Integer allowableFailureNumber = 3;

    /**
     * 表单登录错误使用的验证码类型
     */
    private String formLoginFailureCaptchaType = "picture";

    /**
     * app 登陆错误使用的验证码类型
     */
    private String appLoginFailureCaptchaType = "picture";

    /**
     * 短信 token 参数名称，用于手机号码认证错误时，
     * 在次发送短信验证码时，需要 mobileFailureCaptchaType 类型的验证码通过才能发送短信验证码
     */
    private String smsCaptchaParamName = "_smsCaptchaToken";

    /**
     * 超级管理登陆账户
     */
    private String adminUsername = "admin";

    /**
     * 允许登陆失败次数的缓存配置
     */
    private CacheProperties allowableFailureNumberCache = CacheProperties.of(
            "dactiv:saas:authentication:failure:",
            TimeProperties.of(1800, TimeUnit.SECONDS)
    );

    /**
     * 登出连接
     */
    private String logoutUrl = DEFAULT_LOGOUT_URL;

    /**
     * 需要验证码的认证类型
     */
    private List<String> captchaAuthenticationTypes = DEFAULT_CAPTCHA_AUTHENTICATION_TYPES;

    /**
     * id 解析器类型
     * ignorePrincipalPlugin
     */
    private String ipResolverType = "aliYun";

    /**
     * 忽略的插件服务集合
     */
    private List<String> ignorePluginService = List.of(SystemConstants.SYS_GATEWAY_NAME);

    /**
     * 忽略当前用户的插件集合
     */
    private Map<String, List<String>> ignorePrincipalResource = new LinkedHashMap<>();

    /**
     * token 名称
     */
    private String wakeUpParamName = "token";

    /**
     * 缓存配置
     */
    private CacheProperties wakeUpCache = new CacheProperties(
            "dactiv:saas:app:authentication:mobile:token:",
            new TimeProperties(7, TimeUnit.DAYS)
    );

    private int randomPasswordCount = 10;
    
    private String mobileAuthenticationSecretKey = "";

    private String aliYunIpResolverAppCode = "";

}
