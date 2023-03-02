package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 表单状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FormStatusEnum implements NameValueEnum<Integer> {

    /**
     * 待发布
     */
    NEW(10, "待发布"),
    /**
     * 已发布
     */
    PUBLISH(20, "已发布"),
    /**
     * 已经作废
     */
    DELETED(30, "已作废"),

    ;

    private final Integer value;

    private final String name;

    public static final List<FormStatusEnum> DELETE_STATUS = List.of(NEW, DELETED);
}
