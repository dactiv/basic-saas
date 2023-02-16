package com.github.dactiv.saas.commons.enumeration;


import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 教师类型
 *
 * @author maurice
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeacherTypeEnum implements NameValueEnum<Integer> {

    /**
     * 班主任
     */
    CLASS_TEACHER(10, "班主任"),

    /**
     * 任课老师
     */
    TEACHER(20, "任课老师"),
    ;

    private final Integer value;

    private final String name;
}
