package com.github.dactiv.saas.commons.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期范围描述
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class DateRangeDescribeResponseBody<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 8047722850170411786L;

    /**
     * 数据内容
     */
    Map<String, T> data;

    /**
     * 时间范围
     */
    @JsonIgnoreProperties({"comparator", "naturalOrdering"})
    private Range<Date> range;

    public static <T> DateRangeDescribeResponseBody<T> ofLocalStringDateSort(Map<String, T> data, Range<Date> range, T emptyValue) {
        fillSortStringDate(data, range, emptyValue);
        Map<String, T> value = new TreeMap<>(Comparator.naturalOrder());
        value.putAll(data);

        return DateRangeDescribeResponseBody.of(value, range);
    }

    public static <T> DateRangeDescribeResponseBody<T> ofLocalDateSort(Map<LocalDate, T> data, Range<Date> range, T emptyValue) {
        fillSortLocalDate(data, range, emptyValue);
        Map<LocalDate, T> sort = new TreeMap<>(Comparator.naturalOrder());
        sort.putAll(data);
        Map<String, T> value = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, T> entry : sort.entrySet()) {
            value.put(entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE), entry.getValue());
        }

        return DateRangeDescribeResponseBody.of(value, range);
    }

    public static <T> void fillSortStringDate(Map<String, T> data, Range<Date> range, T emptyValue) {
        Map<LocalDate, T> temp = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : data.entrySet()) {
            temp.put(LocalDate.parse(entry.getKey()), entry.getValue());
        }
        fillSortLocalDate(temp, range, emptyValue);

        for (Map.Entry<LocalDate, T> entry : temp.entrySet()) {
            data.put(entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE), entry.getValue());
        }
    }

    public static <T> void fillSortLocalDate(Map<LocalDate, T> data, Range<Date> range, T emptyValue) {
        List<LocalDate> listKey = new LinkedList<>(data.keySet());

        if (CollectionUtils.isEmpty(listKey)) {
            LocalDate startDate = LocalDate.ofInstant(range.getMinimum().toInstant(), ZoneId.systemDefault());
            LocalDate endDate = LocalDate.ofInstant(range.getMaximum().toInstant(), ZoneId.systemDefault());
            long end = endDate.toEpochDay() - startDate.toEpochDay();
            for (int i = 0; i <= end; i++) {
                LocalDate day = startDate.plusDays(i);
                data.put(day, emptyValue);
            }
            return;
        }

        LocalDate min = listKey.stream().min(Comparator.naturalOrder()).orElse(null);
        if (Objects.nonNull(min)) {
            long length = min.toEpochDay() - LocalDate.ofInstant(range.getMinimum().toInstant(), ZoneId.systemDefault()).toEpochDay();
            for (long i = 1; i <= length; i++) {
                LocalDate day = min.minusDays(i);
                data.put(day, emptyValue);
            }
        }

        LocalDate max = listKey.stream().max(Comparator.naturalOrder()).orElse(null);
        if (Objects.nonNull(max)) {
            long length = LocalDate.ofInstant(range.getMaximum().toInstant(), ZoneId.systemDefault()).toEpochDay() - max.toEpochDay();
            for (long i = 1; i <= length; i++) {
                LocalDate day = max.plusDays(i);
                data.put(day, emptyValue);
            }
        }

        for (int i = 0; i < listKey.size() - 1; i++) {
            LocalDate target = listKey.get(i);
            LocalDate source = listKey.get(i + 1);
            long length = source.toEpochDay() - target.toEpochDay();
            for (long j = 1; j < length; j++) {
                LocalDate day = target.plusDays(j);
                data.put(day, emptyValue);
            }
        }
    }

}
