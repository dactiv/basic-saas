package com.github.dactiv.saas.authentication.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.dactiv.saas.commons.config.SchoolProperties;
import com.github.dactiv.saas.commons.config.WechatProperties;
import com.github.dactiv.saas.commons.service.WechatService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
public class ApplicationStartupAutoConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    @Bean
    @ConfigurationProperties("dactiv.saas.authentication.app.school")
    public SchoolProperties SchoolProperties() {
        return new SchoolProperties();
    }

    @Bean
    @ConfigurationProperties("dactiv.saas.authentication.app.wechat")
    public WechatProperties wechatProperties() {
        return new WechatProperties();
    }

    @Bean
    public WechatService wechatService(WechatProperties wechatProperties, RestTemplate restTemplate, RedissonClient redissonClient) {
        return new WechatService(wechatProperties, restTemplate, redissonClient);
    }

}
