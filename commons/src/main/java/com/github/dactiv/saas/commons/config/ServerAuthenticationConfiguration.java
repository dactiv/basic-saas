package com.github.dactiv.saas.commons.config;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationTypeTokenResolver;
import feign.RequestInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

/**
 * 服务 api 认真的 feign 配置，用于调用服务前自动添加头和认真信息使用
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class ServerAuthenticationConfiguration {

    public static final String SCHOOL_SERVER_API_TYPE = "SCHOOL";

    private static final AesCipherService cipherService = new AesCipherService();

    @Bean
    public RequestInterceptor feignAuthRequestInterceptor(AuthenticationProperties properties, SchoolProperties schoolProperties) {
        return requestTemplate -> {
            requestTemplate.target(schoolProperties.getApiUrl());
            HttpHeaders httpHeaders = of(properties, schoolProperties);
            httpHeaders.forEach((k, v) -> requestTemplate.header(k, v.iterator().next()));

        };
    }

    public static String encodeUserProperties(AuthenticationProperties properties, SchoolProperties schoolProperties) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        ByteSource password = cipherService.encrypt(
                schoolProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                Base64.decode(schoolProperties.getAccessKey())
        );

        requestBody.add(properties.getUsernameParamName(), schoolProperties.getCode());
        requestBody.add(properties.getPasswordParamName(), password.getBase64());

        String token = Casts.castRequestBodyMapToString(requestBody);

        return Base64.encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构造 feign 认证的 http headers
     *
     * @param properties 认证配置信息
     * @return feign 认证的 http headers
     */
    public static HttpHeaders of(AuthenticationProperties properties, SchoolProperties schoolProperties) {
        HttpHeaders httpHeaders = new HttpHeaders();

        String base64 = encodeUserProperties(properties, schoolProperties);

        httpHeaders.add(properties.getTypeHeaderName(), SCHOOL_SERVER_API_TYPE);
        httpHeaders.add(properties.getTokenHeaderName(), base64);
        httpHeaders.add(properties.getTokenResolverHeaderName(), FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE);

        return httpHeaders;
    }
}
