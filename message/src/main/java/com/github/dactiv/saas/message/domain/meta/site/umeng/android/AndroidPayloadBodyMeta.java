package com.github.dactiv.saas.message.domain.meta.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 友盟安卓 Payload Body 实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayloadBodyMeta {

    private String ticker;

    private String title;

    private String text;

    private String icon;

    private String img;

    private String sound;

    private String builderId;

    private boolean playVibrate;

    private boolean playLights;

    private boolean playSound;

    private String afterOpen;

    private String url;

    private String activity;

    private Map<String, Object> custom;
}
