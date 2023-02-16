package com.github.dactiv.saas.message.config.site.umeng;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ios 配置信息
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class Ios {

    public final static String NAME = "IOS";

    private String appKey;

    private String secretKey;
}
