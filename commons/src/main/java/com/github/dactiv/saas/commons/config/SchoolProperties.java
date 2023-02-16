package com.github.dactiv.saas.commons.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 学校配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
public class SchoolProperties {

    /**
     * id 信息
     */
    private Integer id = 1;

    /**
     * 学校名称
     */
    private String name = "本地学校";

    /**
     * 学校代码
     */
    private String code = "localhost";

    /**
     * 学校域名
     */
    private String domain = "http://localhost:8080";

    /**
     * 服务 api 地址
     */
    private String apiUrl = "http://localhost:9080";

    /**
     * 分数等级范围
     */
    private List<ScoreLevelRangeProperties> scoreLevelRange = List.of(
            ScoreLevelRangeProperties.of(1, "A+", 95.00, 100.00),
            ScoreLevelRangeProperties.of(2, "A", 85.00, 95.00),
            ScoreLevelRangeProperties.of(3, "B", 75.00, 85.00),
            ScoreLevelRangeProperties.of(4, "B", 60.00, 75.00),
            ScoreLevelRangeProperties.of(5, "D", 0.00, 60.00)
    );

    /**
     * 教师邀请缓存配置
     *
     * @deprecated 暂时没用到
     */
    @Deprecated
    private CacheProperties teacherInviteCache = CacheProperties.of(
            "dactiv:saas:teacher:invite",
            TimeProperties.of(30, TimeUnit.MINUTES)
    );

    /**
     * 访问 key
     */
    private String accessKey = "";

    /**
     * 安全 key
     */
    private String secretKey = "";

    /**
     * 学期接续时间
     */
    private MonthDay semesterCompleteMonthDay = MonthDay.of(8, 1);

    /**
     * 学段后缀
     */
    private String periodSuffix = "年";

    public Range<Date> getDefaultTrendIntervalDay(Date startTime, Date endTime) {
        LocalDate now = LocalDate.now();

        if (Objects.isNull(startTime)) {
            LocalDate startDate;
            if (MonthDay.from(now).isAfter(semesterCompleteMonthDay)) {
                startDate = now.withDayOfMonth(semesterCompleteMonthDay.getDayOfMonth());
            } else {
                startDate = now.withDayOfMonth(semesterCompleteMonthDay.getDayOfMonth()).withYear(now.getYear() - 1);
            }
            startTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (Objects.isNull(endTime)) {
            LocalDate endDate;
            if (MonthDay.from(now).isAfter(semesterCompleteMonthDay)) {
                endDate = now.withDayOfMonth(semesterCompleteMonthDay.getDayOfMonth()).withYear(now.getYear() + 1);
            } else {
                endDate = now;
            }
            endTime = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        return Range.between(startTime, endTime);
    }
}
