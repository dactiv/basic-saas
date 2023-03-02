package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.saas.authentication.security.handler.CaptchaAuthenticationFailureResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证码认证 filter 实现
 *
 * @author maurice.chen
 */
@Slf4j
public class CaptchaAuthenticationFilter extends RequestAuthenticationFilter implements Ordered {

    private final CaptchaAuthenticationFailureResponse handler;

    public CaptchaAuthenticationFilter(AuthenticationProperties properties,
                                       List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                       List<UserDetailsService<?>> userDetailsServices,
                                       CaptchaAuthenticationFailureResponse handler) {
        super(properties, authenticationTypeTokenResolvers, userDetailsServices);
        this.handler = handler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        // 判断是否需要验证码授权
        if (handler.isCaptchaAuthentication(request)) {

            Map<String, Object> params = new LinkedHashMap<>();

            request.getParameterMap().forEach((k, v) -> {

                if (v.length > 1) {
                    params.put(k, Arrays.asList(v));
                } else {
                    params.put(k, v[0]);
                }

            });

            try {
                RestResult<Map<String, Object>> restResult = handler.getConfigServiceFeignClient().verifyCaptcha(params);

                if (restResult.getStatus() != HttpStatus.OK.value()) {
                    throw new BadCredentialsException(restResult.getMessage());
                }
            } catch (Exception e) {
                log.error("调用校验验证码服务发生异常", e);
                throw new BadCredentialsException("验证码错误");
            }

        }

        return super.attemptAuthentication(request, response);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
