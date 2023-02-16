package com.github.dactiv.saas.config.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 综合素质评价类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClassCertificateTypeEnum implements NameValueEnum<Integer> {

    /**
     * 流动红旗
     */
    MOBILE_RED_BANNER(10, "流动红旗", "mobileRedBanner"),

    ;

    private final Integer value;

    private final String name;

    private final String code;
}
