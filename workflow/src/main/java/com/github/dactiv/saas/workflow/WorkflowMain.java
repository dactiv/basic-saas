package com.github.dactiv.saas.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableWebSecurity
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableFeignClients("com.github.dactiv.saas.commons.feign")
@EnableRedisHttpSession(redisNamespace = "cmis:spring:session")
@EnableMethodSecurity(securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.github.dactiv.saas.workflow")
public class WorkflowMain {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowMain.class, args);
    }
}
