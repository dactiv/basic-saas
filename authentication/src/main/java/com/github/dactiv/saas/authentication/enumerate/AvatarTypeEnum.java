package com.github.dactiv.saas.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 头像类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AvatarTypeEnum implements NameValueEnum<String> {

    /**
     * 用户头像
     */
    USER("user", "用户头像"),
    /**
     * 教师头像
     */
    TEACHER("teacher", "教师头像"),
    /**
     * 班级头像
     */
    CLASS_GRADES("class_grades", "班级头像");

    private final String value;

    private final String name;
}
