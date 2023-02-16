package com.github.dactiv.saas.commons.domain.meta.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.TreeMap;

/**
 * 微信模版消息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class TemplateMessageMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 71381136214265923L;

    public static final String ITEM_VALUE_FIELD_NAME = "value";

    public static final String ITEM_COLOR_FIELD_NAME = "color";

    /**
     * 微信 open id
     */
    @NonNull
    @JsonProperty("touser")
    private String openId;
    /**
     * 模版 id
     */
    @NonNull
    @JsonProperty("template_id")
    private String templateId;

    /**
     * data数据
     */
    private TreeMap<String, TreeMap<String, String>> data;

    /**
     * 参数
     *
     * @param value 值
     * @param color 颜色 可不填
     * @return params
     */
    public static TreeMap<String, String> item(String value, String color) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put(ITEM_VALUE_FIELD_NAME, value);
        params.put(ITEM_COLOR_FIELD_NAME, color);
        return params;
    }
}
