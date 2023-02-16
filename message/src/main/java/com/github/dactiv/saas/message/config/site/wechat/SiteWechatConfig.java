package com.github.dactiv.saas.message.config.site.wechat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 站内信微信配置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class SiteWechatConfig {

    public static final String APPLET_TEMPLATE_ID_FIELD_NAME = "wechatAppletTemplateId";

    public static final String OFFICIAL_TEMPLATE_ID_FIELD_NAME = "wechatOfficialTemplateId";

    /**
     * 小程序站内信模版 id
     */
    private String appletSiteTemplateId;

    /**
     * 公众号站内信模版 id
     */
    private String officialSiteTemplateId;

    private List<String> supportIdFieldName = List.of(APPLET_TEMPLATE_ID_FIELD_NAME, OFFICIAL_TEMPLATE_ID_FIELD_NAME);
}
