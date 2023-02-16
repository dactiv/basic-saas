package com.github.dactiv.saas.message.domain.meta.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓 Payload 实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayloadMeta {

    private String displayType;

    private AndroidPayloadBodyMeta body;

    private Map<String, Object> extra = new LinkedHashMap<>();
}
