package com.github.dactiv.saas.commons.domain.meta.wechat;

import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class SimpleWechatUserDetailsMeta implements WechatUserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = -403945100441873434L;

    public static final String SESSION_KEY_FIELD_NAME = "sessionKey";

    public static final String OPEN_ID_FIELD_NAME = "openId";

    public static final String UNION_ID_FIELD_NAME = "unionId";

    /**
     * session key
     */
    private String sessionKey;

    /**
     * 用户唯一标识
     */
    private String openId;

    /**
     * 用户在开放平台的唯一标识符
     */
    private String unionId;

    public static SimpleWechatUserDetailsMeta of(Map<String, Object> map) {
        SimpleWechatUserDetailsMeta meta = new SimpleWechatUserDetailsMeta();

        meta.setSessionKey(map.get("session_key").toString());
        meta.setOpenId(map.get("openid").toString());

        meta.setUnionId(map.getOrDefault("unionid", StringUtils.EMPTY).toString());

        return meta;
    }
}
