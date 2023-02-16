package com.github.dactiv.saas.message.config.site.umeng;

import com.github.dactiv.saas.commons.enumeration.MessageTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 安卓配置信息实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class Android {

    public final static String NAME = "ANDROID";

    private String appKey;

    private String secretKey;

    private boolean push;

    private String activity;

    private List<MessageTypeEnum> ignoreActivityType = new ArrayList<>();

}
