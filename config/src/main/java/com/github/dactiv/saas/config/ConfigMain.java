package com.github.dactiv.saas.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableWebSecurity
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableMethodSecurity(securedEnabled = true)
@EnableFeignClients("com.github.dactiv.saas.commons.feign")
@SpringBootApplication(scanBasePackages = "com.github.dactiv.saas.config")
public class ConfigMain {

    public static void main(String[] args) {
        SpringApplication.run(ConfigMain.class, args);
    }
}
