package com.github.dactiv.saas.commons.domain.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dactiv.saas.commons.domain.meta.wechat.TemplateMessageMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serial;

/**
 * 小程序订阅消息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppletSubscribeMessageDto extends TemplateMessageMeta {

    @Serial
    private static final long serialVersionUID = -5067306773567105399L;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
     */
    private String page;

    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    @JsonProperty("miniprogram_state")
    private String programState;

    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    private String lang = "zh_CN";

    public static AppletSubscribeMessageDto of(@NonNull String openId, @NonNull String templateId) {
        AppletSubscribeMessageDto result = new AppletSubscribeMessageDto();

        result.setOpenId(openId);
        result.setTemplateId(templateId);

        return result;
    }

    public static AppletSubscribeMessageDto of(@NonNull String openId, @NonNull String templateId, String page) {
        AppletSubscribeMessageDto result = AppletSubscribeMessageDto.of(openId, templateId);
        result.setPage(page);

        return result;
    }

}
