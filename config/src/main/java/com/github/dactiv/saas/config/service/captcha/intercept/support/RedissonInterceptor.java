package com.github.dactiv.saas.config.service.captcha.intercept.support;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.saas.config.config.CaptchaConfig;
import com.github.dactiv.saas.config.service.captcha.BuildToken;
import com.github.dactiv.saas.config.service.captcha.CaptchaService;
import com.github.dactiv.saas.config.service.captcha.DelegateCaptchaService;
import com.github.dactiv.saas.config.service.captcha.intercept.Interceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * redis 实现的验证码拦截器
 *
 * @author maurice
 */
@Component
@RefreshScope
public class RedissonInterceptor implements Interceptor {

    private final RedissonClient redissonClient;

    private final DelegateCaptchaService delegateCaptchaService;

    private final CaptchaConfig captchaConfig;

    public RedissonInterceptor(RedissonClient redissonClient,
                               CaptchaConfig captchaConfig,
                               DelegateCaptchaService delegateCaptchaService) {
        this.redissonClient = redissonClient;
        this.captchaConfig = captchaConfig;
        this.delegateCaptchaService = delegateCaptchaService;
    }

    @Override
    public BuildToken generateCaptchaIntercept(String token, String type, String interceptType) {

        // 通过 token 值获取验证码服务
        Optional<CaptchaService> optional = delegateCaptchaService.getCaptchaServices()
                .stream()
                .filter(c -> {
                    try {
                        BuildToken t = c.getBuildToken(token);

                        if (t == null) {
                            return false;
                        }

                        return t.getType().equals(interceptType);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();

        CaptchaService tokenCaptchaService = optional.orElseThrow(() -> new ServiceException("找不到 token 为[" + token + "]的记录"));

        // 获取当前要拦截的验证码绑定 token
        BuildToken exist = tokenCaptchaService.getBuildToken(token);

        if (exist == null) {
            throw new ServiceException("找不到 token 为 [" + token + "] 的记录");
        }

        // 通过类型，创建一个验证码拦截 token，并用被拦截的 token id 做唯一 id 值，
        // 这样在校验时，可以直接通过该值去要拦截的 token
        CaptchaService captchaService = delegateCaptchaService.getCaptchaServiceByType(type);

        BuildToken buildToken = captchaService.generateToken(exist.getId());
        // 关联一次 token，在校验被拦截的 token 生成验证码时，可以直接获取该值去校验要拦截的验证码信息
        exist.setInterceptToken(buildToken);
        // 保存一次被拦截的 token
        tokenCaptchaService.saveBuildToken(exist);
        // 保存一次要拦截的 token
        saveInterceptToken(exist);
        // 响应信息给前端根据构造信息创建验证码。
        return buildToken;
    }

    /**
     * 保存拦截 token
     *
     * @param token 绑定 token
     */
    public void saveInterceptToken(BuildToken token) {
        RBucket<BuildToken> buildTokenBucket = getBuildTokenBucket(token.getType(), token.getToken().getName());
        buildTokenBucket.setAsync(token, token.getToken().getExpiresTime().getValue(), token.getToken().getExpiresTime().getUnit());
    }

    /**
     * 获取拦截 token
     *
     * @param type  类型
     * @param token token 值
     * @return 绑定 token
     */
    public BuildToken getInterceptToken(String type, String token) {
        RBucket<BuildToken> bucket = getBuildTokenBucket(type, token);
        return bucket.get();
    }

    /**
     * 获取绑定 token 桶
     *
     * @param type  类型
     * @param token token 值
     * @return 绑定 token 桶
     */
    public RBucket<BuildToken> getBuildTokenBucket(String type, String token) {
        return redissonClient.getBucket(getInterceptTokenKey(type, token));
    }

    /**
     * 获取拦截验证码的绑定 token key 名称
     *
     * @param type  拦截类型
     * @param token token 值
     * @return 名称
     */
    protected String getInterceptTokenKey(String type, String token) {
        return captchaConfig.getInterceptorTokenCache().getName(type + ":" + token);
    }

    @Override
    public RestResult<Map<String, Object>> verifyCaptcha(HttpServletRequest request) {

        // 通过本次请求看看是否需要做一次拦截验证
        CaptchaService captchaService = delegateCaptchaService.getCaptchaServiceByRequest(request);

        String token = request.getParameter(captchaService.getTokenParamName());

        BuildToken interceptToken = getInterceptToken(captchaService.getType(), token);
        // 如果找不到 token 表示不需要拦截，可以直接生成验证码
        if (interceptToken == null) {
            return RestResult.of("token [" + token + "] 无拦截");
        }

        // 如果不为空，获取要拦截的验证码服务
        CaptchaService interceptCaptchaService = delegateCaptchaService
                .getCaptchaServiceByType(interceptToken.getInterceptToken().getType());
        // 获取提交验证码的参数名
        String paramName = interceptCaptchaService.getCaptchaParamName();
        // 校验验证码
        RestResult<Map<String, Object>> result = interceptCaptchaService.verify(interceptToken, request.getParameter(paramName));
        // 如果成功，删除拦截 token
        if (result.getStatus() == HttpStatus.OK.value()) {
            deleteInterceptToken(captchaService.getType(), token);
        }

        return result;
    }

    /**
     * 删除拦截验证码
     *
     * @param type  拦截类型
     * @param token 绑定 token 值
     */
    private void deleteInterceptToken(String type, String token) {
        getBuildTokenBucket(type, token).deleteAsync();
    }
}
