package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;

/**
 * 星期枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WeekEnum implements NameValueEnum<Integer> {

    /**
     * 星期一
     */
    MONDAY(1, "星期一", DayOfWeek.MONDAY, "周一"),

    /**
     * 星期二
     */
    TUESDAY(2, "星期二", DayOfWeek.TUESDAY, "周二"),

    /**
     * 星期三
     */
    WEDNESDAY(3, "星期三", DayOfWeek.WEDNESDAY, "周三"),

    /**
     * 星期四
     */
    THURSDAY(4, "星期四", DayOfWeek.THURSDAY, "周四"),

    /**
     * 星期五
     */
    FRIDAY(5, "星期五", DayOfWeek.FRIDAY, "周五"),

    /**
     * 星期六
     */
    SATURDAY(6, "星期六", DayOfWeek.SATURDAY, "周六"),

    /**
     * 星期天
     */
    SUNDAY(0, "星期天", DayOfWeek.SUNDAY, "周日"),

    ;

    private final Integer value;

    private final String name;

    private final DayOfWeek dayOfWeek;

    private final String shortName;
}
