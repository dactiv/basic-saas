package com.github.dactiv.saas.message.domain.meta.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.saas.message.domain.meta.site.umeng.BasicMessageMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓消息实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidMessageMeta extends BasicMessageMeta {

    private boolean mipush;

    private String miActivity;

    private Map<String, Object> channelProperties = new LinkedHashMap<>();

}
