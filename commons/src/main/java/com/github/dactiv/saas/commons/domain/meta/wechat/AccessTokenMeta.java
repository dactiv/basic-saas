package com.github.dactiv.saas.commons.domain.meta.wechat;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 微信访问 token
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class AccessTokenMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -6748349389161375941L;

    /**
     * token 值
     */
    private String token;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 过期时间
     */
    private TimeProperties expiresTime;
}
