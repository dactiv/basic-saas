package com.github.dactiv.saas.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 部门类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DepartmentTypeEnum implements NameValueEnum<Integer> {

    /**
     * 后台管理员
     */
    CONSOLE_USER(10, "后台管理员"),

    ;

    private final Integer value;

    private final String name;
}
