package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.saas.workflow.service.schedule.FormDateResolver;
import com.github.dactiv.saas.workflow.service.schedule.support.DateAndTimeFormResolver;
import com.github.dactiv.saas.workflow.service.schedule.support.DateFormResolver;
import com.github.dactiv.saas.workflow.service.schedule.support.DatetimeFormResolver;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 日程表单时间类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ScheduleFormDateTypeEnum implements NameValueEnum<Integer> {
    /**
     * 时间类型
     */
    DATE(10, "日期类型", DateFormResolver.class),

    /**
     * 时间类型
     */
    DATETIME(30, "日期时间类型", DatetimeFormResolver.class),

    /**
     * 时间与时间两个字段合起来的类型
     */
    DATE_AND_TIME(40, "时间与时间两个字段合起来的类型", DateAndTimeFormResolver.class),

    ;

    private final Integer value;

    private final String name;

    private final Class<? extends FormDateResolver> formDateResolverClass;
}
