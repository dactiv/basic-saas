package com.github.dactiv.saas.commons.enumeration;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 时间周期枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DatePeriodEnum {

    /**
     * 周
     */
    WEEK(10, "周"),
    /**
     * 月
     */
    MONTH(20, "月"),
    /**
     * 季度
     */
    QUARTER(30, "季度"),
    /**
     * 半年
     */
    HALF_A_YEAR(40, "半年"),
    /**
     * 年
     */
    YEAR(50, "年"),
    ;

    private final Integer value;

    private final String name;

    public Range<Date> getDatePeriod() {
        return getDatePeriod(LocalDate.now());
    }

    public Range<Date> getDatePeriod(LocalDate today) {
        if (WEEK.getValue().equals(getValue())) {
            LocalDateTime start = LocalDateTime.of(today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)), LocalTime.MIN);
            if (DayOfWeek.MONDAY.equals(today.getDayOfWeek())) {
                start = LocalDateTime.of(today, LocalTime.MIN);
            }
            LocalDateTime end = LocalDateTime.of(today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)), LocalTime.MAX);
            return Range.between(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        } else if (MONTH.getValue().equals(getValue())) {
            LocalDateTime start = LocalDateTime.of(LocalDate.of(today.getYear(), today.getMonth(), 1), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(today.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
            return Range.between(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        } else if (QUARTER.getValue().equals(getValue())) {
            Month month = today.getMonth();
            Month firstMonthOfQuarter = month.firstMonthOfQuarter();
            Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);

            LocalDateTime start = LocalDateTime.of(LocalDate.of(today.getYear(), today.getMonth(), 1), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(LocalDate.of(today.getYear(), endMonthOfQuarter, endMonthOfQuarter.length(today.isLeapYear())), LocalTime.MAX);
            return Range.between(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        } else if (HALF_A_YEAR.getValue().equals(getValue())) {
            LocalDateTime start = LocalDateTime.of(LocalDate.of(today.getYear(), Month.JANUARY, 1), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(LocalDate.of(today.getYear(), Month.JUNE, Month.JUNE.length(today.isLeapYear())), LocalTime.MAX);
            return Range.between(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            LocalDateTime start = LocalDateTime.of(LocalDate.of(today.getYear(), Month.JANUARY, 1), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(LocalDate.of(today.getYear(), Month.DECEMBER, Month.DECEMBER.length(today.isLeapYear())), LocalTime.MAX);
            return Range.between(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        }
    }
}
