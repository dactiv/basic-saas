package com.github.dactiv.saas.message.config.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信渠道配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "dactiv.saas.message.sms.ali-yun")
public class AliYunSmsConfig {

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint = "dysmsapi.aliyuncs.com";
}
