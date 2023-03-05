package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 自定义申请类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyFormTypeEnum implements NameValueEnum<Integer> {

    /**
     * 自定义格式表单
     */
    CUSTOM(10, "自定义格式表单"),

    /**
     * 系统默认表单
     */
    SYSTEM(15, "系统默认表单"),
    ;

    private final Integer value;

    private final String name;

    public static final List<ApplyFormTypeEnum> SYSTEM_TYPE = List.of(CUSTOM, SYSTEM);
}
