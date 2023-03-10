package com.github.dactiv.saas.config.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.AlgorithmProperties;
import com.github.dactiv.framework.crypto.RsaProperties;
import com.github.dactiv.saas.commons.SystemConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.config.app")
public class ApplicationConfig {
    
    /**
     * 展示轮播图数量
     */
    private Integer carouselCount = 5;

    private CacheProperties accessCryptoCache = CacheProperties.of(
            "dactiv:saas:access:crypto:all"
    );

    /**
     * 存储在 redis 私有 token 缓存配置
     */
    private CacheProperties privateKeyCache = CacheProperties.of(
            "access:crypto:token:private:",
            new TimeProperties(30, TimeUnit.SECONDS)
    );

    /**
     * 存储在 redis 访问 token 缓存配置
     */
    private CacheProperties accessTokenKeyCache = CacheProperties.of(
            "access:crypto:token:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

    /**
     * 加解密算法配置
     */
    private AlgorithmProperties algorithm;

    /**
     * rsa 配置
     */
    private RsaProperties rsa;

    private String dictionarySeparator = ".";

    /**
     * 忽略环境变量的开头值
     */
    private List<String> ignoreEnvironmentStartWith = List.of("spring");

    /**
     * 忽略的枚举服务集合
     */
    private List<String> ignoreEnumerateService = List.of(SystemConstants.SYS_GATEWAY_NAME);

    /**
     * 用户导入数据缓存
     */
    private CacheProperties userImportCache = CacheProperties.of("dactiv:saas:resources:user:import", TimeProperties.of(7, TimeUnit.DAYS));

    private CacheProperties userExportCache = CacheProperties.of("dactiv:saas:resources:user:export:", TimeProperties.of(7, TimeUnit.DAYS));
}
