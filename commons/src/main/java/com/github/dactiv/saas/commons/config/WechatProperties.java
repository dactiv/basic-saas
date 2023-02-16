package com.github.dactiv.saas.commons.config;

import com.github.dactiv.saas.commons.domain.meta.wechat.WechatAccountMeta;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信配置类
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class WechatProperties {

    public static final String DEFAULT_PHONE_NUMBER_CODE_PARAM_NAME = "phoneNumber";

    /**
     * 小程序账户
     */
    private WechatAccountMeta applet = new WechatAccountMeta();

    /**
     * 公众号账户
     */
    private WechatAccountMeta official = new WechatAccountMeta();

    /**
     * 手机号码参数名称
     */
    private String phoneNumberCodeParamName = DEFAULT_PHONE_NUMBER_CODE_PARAM_NAME;

    /**
     * 请求状态字段名称
     */
    private String statusCodeFieldName = "errcode";

    /**
     * 请求错误消息字段
     */
    private String statusMessageFieldName = "errmsg";

    /**
     * 请求成功匹配值
     */
    private String successCodeValue = "0";

    /**
     * 成功认证后绑定微信参数名称
     */
    private String successAuthenticationBuildParamName = "wechatCode";
}
