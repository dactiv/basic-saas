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
     * 用户头像
     */
    STUDENT(10, "学生"),
    /**
     * 教师头像
     */
    TEACHER(20, "教师"),
    /**
     * 后台管理员
     */
    CONSOLE_USER(30, "后台管理员"),

    ;

    private final Integer value;

    private final String name;
}
