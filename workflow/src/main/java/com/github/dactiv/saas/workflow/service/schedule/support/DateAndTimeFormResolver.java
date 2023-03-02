package com.github.dactiv.saas.workflow.service.schedule.support;

import com.github.dactiv.saas.workflow.service.schedule.FormDateResolver;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 日期与时间两个字段的表单解析器实现
 *
 * @author maurice.chen
 */
public class DateAndTimeFormResolver implements FormDateResolver {

    public static final String START_DATE_FIELD = "startDate";

    public static final String START_TIME_FIELD = "startTime";

    public static final String END_TIME_FIELD = "endTime";

    public static final String END_DATE_FIELD = "endDate";

    @Override
    public Date getStartDate(Map<String, Object> applyContent) {
        return getDate(applyContent, START_DATE_FIELD, START_TIME_FIELD);
    }

    @Override
    public Date getEndDate(Map<String, Object> applyContent) {
        return getDate(applyContent, END_DATE_FIELD, END_TIME_FIELD);
    }

    private Date getDate(Map<String, Object> applyContent, String endDateField, String endTimeField) {
        String endDateValue = applyContent.get(endDateField).toString();
        String endTimeValue = applyContent.get(endTimeField).toString();

        LocalDate endDate = null;
        LocalTime endTime = null;

        if (StringUtils.isNotBlank(endDateValue)) {
            endDate = LocalDate.parse(endDateValue);
        }

        if (StringUtils.isNotBlank(endTimeValue)) {
            endTime = LocalTime.parse(endTimeValue);
        }

        if (Objects.nonNull(endDate) && Objects.nonNull(endTime)) {
            LocalDateTime localDateTime = LocalDateTime.of(endDate, endTime);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        return null;
    }
}
