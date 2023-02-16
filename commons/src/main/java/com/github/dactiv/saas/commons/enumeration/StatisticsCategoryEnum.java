package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计类别类别枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatisticsCategoryEnum implements NameValueEnum<Integer> {

    /**
     * 在线浏览
     */
    ONLINE_VIEW_COUNT(10, "在线浏览"),
    /**
     * 下载人次
     */
    DOWNLOADS(20, "下载人次"),
    /**
     * 浏览人次
     */
    VIEWERS(30, "浏览人次"),

    /**
     * 课程主题发布数量
     */
    CURRICULUM_FORUM_TOPIC(40, "课程主题发布数量"),

    /**
     * 课程主题回复数量
     */
    CURRICULUM_FORUM_TOPIC_REPLY(50, "课程主题回复数量"),
    ;
    private final Integer value;

    private final String name;
}