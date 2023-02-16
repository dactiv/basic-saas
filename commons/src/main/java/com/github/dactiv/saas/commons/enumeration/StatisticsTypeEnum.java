package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatisticsTypeEnum implements NameValueEnum<Integer> {

    /**
     * 课程视频
     */
    CURRICULUM_VIDEO(10, "课程视频"),

    /**
     * 课程文档
     */
    CURRICULUM_DOCUMENT(20, "课程文档"),

    /**
     * 课程答疑区
     */
    CURRICULUM_FORUM_ISSUES(30, "课程答疑区"),

    /**
     * 课程讨论区
     */
    CURRICULUM_FORUM_COMMUNICATION(40, "课程讨论区"),

    /**
     * 课程讨论自定义区
     */
    CURRICULUM_FORUM_CUSTOM(50, "课程讨论自定义区");

    private final Integer value;

    private final String name;

}
