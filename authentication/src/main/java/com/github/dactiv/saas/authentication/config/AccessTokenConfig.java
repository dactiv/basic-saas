package com.github.dactiv.saas.authentication.config;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 访问 token 配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.authentication.app.access-token")
public class AccessTokenConfig {

    /**
     * 访问 token 时间与密码分割符
     */
    private String separator = "%$&-%%^";

    /**
     * 超时时间
     */
    private TimeProperties expirationTime = TimeProperties.of(6000, TimeUnit.SECONDS);

}
