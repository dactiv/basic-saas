package com.github.dactiv.saas.commons.domain.meta.wechat;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 微信账户元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class WechatAccountMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 6866875480647438435L;

    /**
     * app id
     */
    private String appId;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 获取 token 提前时间配置
     */
    private TimeProperties getAccessTokenLeadTime = TimeProperties.of(1, TimeUnit.MINUTES);

    /**
     * 访问 token 缓存配置
     */
    private CacheProperties accessTokenCache;

}
