package com.github.dactiv.saas.authentication.config;

import com.github.dactiv.framework.spring.security.SpringSecurityAutoConfiguration;
import com.github.dactiv.framework.spring.security.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.saas.authentication.security.CaptchaAuthenticationFilter;
import com.github.dactiv.saas.authentication.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.saas.authentication.security.handler.CaptchaAuthenticationFailureResponse;
import com.github.dactiv.saas.authentication.security.handler.JsonLogoutSuccessHandler;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@AutoConfigureAfter({SpringSecurityAutoConfiguration.class, RedisHttpSessionConfiguration.class})
public class SpringSecurityConfig<S extends Session> implements WebSecurityConfigurerAfterAdapter {

    private final AuthenticationProperties properties;

    private final CookieRememberService rememberMeServices;

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    private final JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    private final ApplicationConfig applicationConfig;

    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    private final CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse;

    private final ApplicationEventPublisher eventPublisher;

    private final AuthenticationManager authenticationManager;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<UserDetailsService<?>> userDetailsServices;

    private SpringSessionBackedSessionRegistry<S> sessionBackedSessionRegistry;

    public SpringSecurityConfig(AuthenticationProperties properties,
                                CookieRememberService rememberMeServices,
                                JsonLogoutSuccessHandler jsonLogoutSuccessHandler,
                                JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy,
                                ApplicationConfig applicationConfig,
                                JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse,
                                ApplicationEventPublisher eventPublisher,
                                AuthenticationManager authenticationManager,
                                ObjectProvider<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver,
                                ObjectProvider<UserDetailsService<?>> userDetailsServices) {

        this.properties = properties;
        this.rememberMeServices = rememberMeServices;
        this.jsonLogoutSuccessHandler = jsonLogoutSuccessHandler;
        this.jsonAuthenticationSuccessHandler = jsonAuthenticationSuccessHandler;
        this.jsonSessionInformationExpiredStrategy = jsonSessionInformationExpiredStrategy;
        this.applicationConfig = applicationConfig;
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.captchaAuthenticationFailureResponse = captchaAuthenticationFailureResponse;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver.orderedStream().collect(Collectors.toList());
        this.userDetailsServices = userDetailsServices.orderedStream().collect(Collectors.toList());
    }

    @Override
    public void configure(HttpSecurity httpSecurity) {

        CaptchaAuthenticationFilter filter = new CaptchaAuthenticationFilter(
                properties,
                authenticationTypeTokenResolvers,
                userDetailsServices,
                captchaAuthenticationFailureResponse
        );

        filter.setAuthenticationManager(authenticationManager);
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);
        filter.setRememberMeServices(rememberMeServices);

        try {
            httpSecurity
                    .addFilter(filter)
                    .logout()
                    .logoutUrl(applicationConfig.getLogoutUrl())
                    .logoutSuccessHandler(jsonLogoutSuccessHandler)
                    .and()
                    .sessionManagement()
                    .maximumSessions(Integer.MAX_VALUE)
                    .sessionRegistry(sessionBackedSessionRegistry)
                    .expiredSessionStrategy(jsonSessionInformationExpiredStrategy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedissonClient redissonClient) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(new RedissonConnectionFactory(redissonClient));
        return redisTemplate;
    }

    @Bean
    public RedisIndexedSessionRepository redisIndexedSessionRepository(RedisTemplate<String, Object> redisTemplate) {
        return new RedisIndexedSessionRepository(redisTemplate);
    }

    @Bean
    public SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<S> redisIndexedSessionRepository) {
        sessionBackedSessionRegistry = new SpringSessionBackedSessionRegistry<>(redisIndexedSessionRepository);
        return sessionBackedSessionRegistry;
    }

}
