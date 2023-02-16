package com.github.dactiv.saas.message.config;

import com.github.dactiv.framework.idempotent.ConcurrentConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 多邮箱配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.saas.message.mail")
public class MailConfig {

    /**
     * SMTP server host. For instance, `smtp.example.com`.
     */
    private String host;

    /**
     * SMTP server port.
     */
    private Integer port;

    /**
     * 邮箱配置
     */
    private Map<String, MailProperties> accounts;

    /**
     * Protocol used by the SMTP server.
     */
    private String protocol = "smtp";

    /**
     * Default MimeMessage encoding.
     */
    private Charset defaultEncoding = StandardCharsets.UTF_8;

    /**
     * Additional JavaMail Session properties.
     */
    private Map<String, String> properties = new HashMap<>();

    /**
     * Session JNDI name. When set, takes precedence over other Session settings.
     */
    private String jndiName;

    /**
     * 批量消息更新并发配置
     */
    private ConcurrentConfig batchUpdateConcurrent = new ConcurrentConfig("email:message:batch:update");
}
