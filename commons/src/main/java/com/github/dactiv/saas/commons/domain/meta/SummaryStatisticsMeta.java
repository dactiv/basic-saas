package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.BigDecimalScaleProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;

/**
 * 统计元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class SummaryStatisticsMeta<T extends Number, S extends Number> implements Serializable {
    @Serial
    private static final long serialVersionUID = 7232543276015713942L;

    /**
     * 总记录数
     */
    private long count;
    /**
     * 总分
     */
    private S sum;
    /**
     * 最低分
     */
    private T min;
    /**
     * 最高分
     */
    private T max;

    /**
     * 获取平均分
     *
     * @return 平均分
     */
    public double getAverage() {
        return getCount() > 0 ? new BigDecimalScaleProperties().valueOf(getSum().doubleValue() / getCount()) : 0.0d;
    }

    public static SummaryStatisticsMeta<Double, Double> of(DoubleSummaryStatistics statistics) {
        SummaryStatisticsMeta<Double, Double> meta = new SummaryStatisticsMeta<>();

        meta.setCount(statistics.getCount());
        meta.setMax(statistics.getMax());
        meta.setMin(statistics.getMin());
        meta.setSum(statistics.getSum());

        return meta;
    }

    public static SummaryStatisticsMeta<Integer, Long> of(IntSummaryStatistics statistics) {
        SummaryStatisticsMeta<Integer, Long> meta = new SummaryStatisticsMeta<>();

        meta.setCount(statistics.getCount());
        meta.setMax(statistics.getMax());
        meta.setMin(statistics.getMin());
        meta.setSum(statistics.getSum());

        return meta;
    }

    public static SummaryStatisticsMeta<Long, Long> of(LongSummaryStatistics statistics) {
        SummaryStatisticsMeta<Long, Long> meta = new SummaryStatisticsMeta<>();

        meta.setCount(statistics.getCount());
        meta.setMax(statistics.getMax());
        meta.setMin(statistics.getMin());
        meta.setSum(statistics.getSum());

        return meta;
    }
}
