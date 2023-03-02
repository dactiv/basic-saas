package com.github.dactiv.saas.workflow.service.schedule.support;

import com.github.dactiv.saas.workflow.service.schedule.FormDateResolver;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 日期表单解释器实现
 *
 * @author maurice.chen
 */
public class DateFormResolver implements FormDateResolver {

    public static final String START_FIELD = "startDate";

    public static final String END_FIELD = "endDate";

    @Override
    public Date getStartDate(Map<String, Object> applyContent) {
        return getDate(applyContent, START_FIELD);
    }

    @Override
    public Date getEndDate(Map<String, Object> applyContent) {
        return getDate(applyContent, END_FIELD);
    }

    public Date getDate(Map<String, Object> applyContent, String dateField) {
        String value = applyContent.get(dateField).toString();
        if (StringUtils.isNotBlank(value)) {

            LocalDate localTime = LocalDate.parse(value);
            return Date.from(localTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
}
