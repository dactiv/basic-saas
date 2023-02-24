package com.github.dactiv.saas.message.config.sms;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dactiv.saas.message.app.sms")
public class SmsConfig {
    /**
     * 渠道商
     */
    private String channel = "YiMei";

}
