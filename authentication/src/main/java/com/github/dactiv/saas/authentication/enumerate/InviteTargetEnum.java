package com.github.dactiv.saas.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀请对象
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InviteTargetEnum implements NameValueEnum<Integer> {

    /**
     * 教师
     */
    TEACHER(10, "教师"),
    /**
     * 学生
     */
    STUDENT(20, "学生"),

    /**
     * 学生
     */
    PARENT(30, "家长"),
    ;

    private final Integer value;

    private final String name;
}
