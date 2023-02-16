package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计日期范围枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatisticsDateRangEnum implements NameValueEnum<Integer> {

    /**
     * 本周
     */
    WEEK(10, "本周"),

    /**
     * 本月
     */
    MONTH(20, "本月"),

    /**
     * 本学期
     */
    SEMESTER(30, "本学期"),

    /**
     * 本学年
     */
    SCHOOL_YEAR(40, "本学年"),

    /**
     * 历史至今
     */
    ALL(50, "历史至今"),
    ;

    private final Integer value;

    private final String name;
}
