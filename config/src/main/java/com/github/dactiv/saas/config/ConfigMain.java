package com.github.dactiv.saas.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableWebSecurity
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableFeignClients("com.github.dactiv.saas.commons.feign")
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.github.dactiv.saas.config")
public class ConfigMain {

    public static void main(String[] args) {
        SpringApplication.run(ConfigMain.class, args);
    }
}
