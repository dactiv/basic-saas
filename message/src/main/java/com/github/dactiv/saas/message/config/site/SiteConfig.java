package com.github.dactiv.saas.message.config.site;

import com.github.dactiv.framework.idempotent.ConcurrentConfig;
import com.github.dactiv.saas.message.config.site.umeng.SiteUmengConfig;
import com.github.dactiv.saas.message.config.site.wechat.SiteWechatConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 站内信配置
 */
@Data
@Component
@ConfigurationProperties("dactiv.saas.message.app.site")
public class SiteConfig {

    /**
     * 友盟站内信配置
     */
    private SiteUmengConfig umeng;

    /**
     * 微信站内信配置
     */
    private SiteWechatConfig wechat;

    /**
     * 渠道商
     */
    private List<String> channel;

    /**
     * 成功状态
     */
    private List<Integer> successStatus = List.of(HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value());

    /**
     * 批量消息更新并发配置
     */
    private ConcurrentConfig batchUpdateConcurrent = new ConcurrentConfig("dactiv:saas:site:message:batch:update");
}
