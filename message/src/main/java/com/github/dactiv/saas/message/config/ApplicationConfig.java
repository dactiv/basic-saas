package com.github.dactiv.saas.message.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.message.app")
public class ApplicationConfig {

    /**
     * 副标题长度
     */
    public Integer maxSubTitleLength = 50;
}
