package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程表单类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FormTypeEnum implements NameValueEnum<Integer> {

    /**
     * 待处理
     */
    SYSTEM(10, "系统类型"),

    /**
     * 已处理
     */
    CUSTOM(20, "自定义类型"),

    ;

    private final Integer value;

    private final String name;
}
