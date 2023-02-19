package com.github.dactiv.saas.authentication.security.handler;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.enumerate.LoginTypeEnum;
import com.github.dactiv.saas.commons.feign.CaptchaServiceFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
public class CaptchaAuthenticationFailureResponse implements JsonAuthenticationFailureResponse {

    public static final String DEFAULT_TYPE_PARAM_NAME = "type";

    public static final String DEFAULT_MOBILE_CAPTCHA_TYPE = "sms";

    public static final String CAPTCHA_EXECUTE_CODE = "1001";
    public static final String LOGIN_EXECUTE_CODE = "1002";

    private final ApplicationConfig applicationConfig;

    private final AuthenticationProperties properties;

    @Getter
    private final CaptchaServiceFeignClient captchaServiceFeignClient;

    private final RedissonClient redissonClient;

    public CaptchaAuthenticationFailureResponse(ApplicationConfig applicationConfig,
                                                AuthenticationProperties properties,
                                                CaptchaServiceFeignClient captchaServiceFeignClient,
                                                RedissonClient redissonClient) {
        this.applicationConfig = applicationConfig;
        this.properties = properties;
        this.captchaServiceFeignClient = captchaServiceFeignClient;
        this.redissonClient = redissonClient;
    }

    @Override
    public void setting(RestResult<Map<String, Object>> result, HttpServletRequest request, AuthenticationException e) {

        Map<String, Object> data = result.getData();

        if (AuthenticationCredentialsNotFoundException.class.isAssignableFrom(e.getClass())) {
            result.setExecuteCode(LOGIN_EXECUTE_CODE);
            return;
        }

        // 获取错误次数
        Integer number = getAllowableFailureNumber(request);

        String type = request.getHeader(properties.getTypeHeaderName());

        if (applicationConfig.getCaptchaAuthenticationTypes().contains(type)) {
            setAllowableFailureNumber(request, ++number);
        }

        if (number < applicationConfig.getAllowableFailureNumber()) {
            return;
        }

        // 获取设备唯一识别
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String loginType = request.getParameter(DEFAULT_TYPE_PARAM_NAME);

        // 如果登录类型为用户名密码登录的情况下，创建一个生成验证码 token 给客户端生成验证码，
        // 该验证码会通过 CaptchaAuthenticationFilter 进行验证码，详情查看 CaptchaAuthenticationFilter。
        // 如果登录类型为手机短信登录，创建一个生成短信发送验证码的拦截 token 给客户端，
        // 让客户端在页面生成一个验证码，该验证码为发送短信时需要验证的验证码，方式短信被刷行为。
        if (LoginTypeEnum.Mobile.toString().equals(loginType)) {

            String token = request.getParameter(applicationConfig.getSmsCaptchaParamName());

            if (StringUtils.isNotBlank(token)) {
                Map<String, Object> buildToken = captchaServiceFeignClient.createGenerateCaptchaIntercept(
                        token,
                        applicationConfig.getMobileFailureCaptchaType(),
                        DEFAULT_MOBILE_CAPTCHA_TYPE
                );

                data.putAll(buildToken);
            }

        } else {
            Map<String, Object> buildToken = captchaServiceFeignClient.generateToken(
                    applicationConfig.getUsernameFailureCaptchaType(),
                    identified
            );
            data.putAll(buildToken);
        }

        result.setExecuteCode(CAPTCHA_EXECUTE_CODE);
    }

    /**
     * 是否需要验证码认证
     *
     * @param request 请求信息
     * @return true 是，否则 false
     */
    public boolean isCaptchaAuthentication(HttpServletRequest request) {
        String type = request.getHeader(properties.getTypeHeaderName());

        if (!applicationConfig.getCaptchaAuthenticationTypes().contains(type)) {
            return false;
        }
        Integer number = getAllowableFailureNumber(request);
        //String type = request.getParameter(DEFAULT_TYPE_PARAM_NAME);
        return number >= applicationConfig.getAllowableFailureNumber(); //&& applicationConfig.getCaptchaAuthenticationTypes().contains(type);
    }

    /**
     * 删除允许认证失败次数
     *
     * @param request 请求信息
     */
    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = applicationConfig.getAllowableFailureNumberCache().getName(identified);

        redissonClient.getBucket(key).deleteAsync();
    }

    /**
     * 设置允许认证失败次数
     *
     * @param request 请求信息
     * @param number  错误次数
     */
    private void setAllowableFailureNumber(HttpServletRequest request, Integer number) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = applicationConfig.getAllowableFailureNumberCache().getName(identified);
        TimeProperties properties = applicationConfig.getAllowableFailureNumberCache().getExpiresTime();

        if (Objects.nonNull(properties)) {
            redissonClient.getBucket(key).setAsync(number, properties.getValue(), properties.getUnit());
        } else {
            redissonClient.getBucket(key).setAsync(number);
        }
    }

    /**
     * 获取允许认证失败次数
     *
     * @param request 请求信息
     * @return 允许认证失败次数
     */
    public Integer getAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = applicationConfig.getAllowableFailureNumberCache().getName(identified);

        Integer value = redissonClient.<Integer>getBucket(key).get();

        if (value == null) {
            return 0;
        }

        return value;
    }

}
