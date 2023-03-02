package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.*;
import java.util.function.Consumer;

/**
 * 带 id 的统计元数据信息
 *
 * @param <T> 最大最小值类型
 * @param <S> 统计数值类型
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class IdSummaryStatisticsMeta<I, T extends Number, S extends Number> extends SummaryStatisticsMeta<T, S> implements BasicIdentification<I> {
    @Serial
    private static final long serialVersionUID = -1937201389731990616L;

    /**
     * 主键 id
     */
    private I id;

    public static <I, T extends Number, S extends Number> IdSummaryStatisticsMeta<I, T, S> of(I id, SummaryStatisticsMeta<T, S> meta) {
        IdSummaryStatisticsMeta<I, T, S> result = new IdSummaryStatisticsMeta<>();

        result.setId(id);
        result.setCount(meta.getCount());
        result.setMax(meta.getMax());
        result.setMin(meta.getMin());
        result.setSum(meta.getSum());

        return result;
    }

    public static <PK> List<IdSummaryStatisticsMeta<PK, Integer, Long>> sortIntSummaryStatistics(Map<PK, IntSummaryStatistics> summaryStatisticsMap) {
        List<IdSummaryStatisticsMeta<PK, Integer, Long>> statisticsMetas = new LinkedList<>();
        summaryStatisticsMap.forEach((k, v) -> statisticsMetas.add(IdSummaryStatisticsMeta.of(k, of(v))));

        return statisticsMetas
                .stream()
                .sorted(Comparator.comparing(SummaryStatisticsMeta::getSum))
                .toList();
    }

    public static <PK> List<IdSummaryStatisticsMeta<PK, Double, Double>> sortDoubleSummaryStatistics(Map<PK, DoubleSummaryStatistics> summaryStatisticsMap) {
        List<IdSummaryStatisticsMeta<PK, Double, Double>> statisticsMetas = new LinkedList<>();
        summaryStatisticsMap.forEach((k, v) -> statisticsMetas.add(IdSummaryStatisticsMeta.of(k, of(v))));

        return statisticsMetas
                .stream()
                .sorted(Comparator.comparing(SummaryStatisticsMeta::getSum))
                .toList();
    }

    public static <PK> List<IdSummaryStatisticsMeta<PK, Long, Long>> sortLongSummaryStatistics(Map<PK, LongSummaryStatistics> summaryStatisticsMap) {
        List<IdSummaryStatisticsMeta<PK, Long, Long>> statisticsMetas = new LinkedList<>();
        summaryStatisticsMap.forEach((k, v) -> statisticsMetas.add(IdSummaryStatisticsMeta.of(k, of(v))));

        return statisticsMetas
                .stream()
                .sorted(Comparator.comparing(SummaryStatisticsMeta::getSum))
                .toList();
    }

    public static <PK> void generateDoubleSummaryStatisticsRanking(Map<PK, DoubleSummaryStatistics> summaryStatisticsMap,
                                                                   Consumer<IdRankingMeta<PK>> consumer) {
        List<IdSummaryStatisticsMeta<PK, Double, Double>> sortList = IdSummaryStatisticsMeta
                .sortDoubleSummaryStatistics(summaryStatisticsMap);

        double currentScore = Double.MAX_VALUE;
        int currentRanking = 0;
        for (int i = sortList.size() - 1; i >= 0; i--) {
            IdSummaryStatisticsMeta<PK, Double, Double> meta = sortList.get(i);
            if (meta.getSum() < currentScore) {
                currentRanking++;
                currentScore = meta.getSum();
            }
            consumer.accept(IdRankingMeta.of(meta.getId(), currentRanking));
        }
    }

    public static <PK> void generateLongSummaryStatisticsRanking(Map<PK, LongSummaryStatistics> summaryStatisticsMap,
                                                                 Consumer<IdRankingMeta<PK>> consumer) {
        List<IdSummaryStatisticsMeta<PK, Long, Long>> sortList = IdSummaryStatisticsMeta
                .sortLongSummaryStatistics(summaryStatisticsMap);

        long currentScore = Integer.MAX_VALUE;
        int currentRanking = 0;
        for (int i = sortList.size() - 1; i >= 0; i--) {
            IdSummaryStatisticsMeta<PK, Long, Long> meta = sortList.get(i);
            if (meta.getSum() < currentScore) {
                currentRanking++;
                currentScore = meta.getSum();
            }
            consumer.accept(IdRankingMeta.of(meta.getId(), currentRanking));
        }
    }

    public static <PK> void generateIntSummaryStatisticsRanking(Map<PK, IntSummaryStatistics> summaryStatisticsMap,
                                                                Consumer<IdRankingMeta<PK>> consumer) {
        List<IdSummaryStatisticsMeta<PK, Integer, Long>> sortList = IdSummaryStatisticsMeta
                .sortIntSummaryStatistics(summaryStatisticsMap);

        long currentScore = Integer.MAX_VALUE;
        int currentRanking = 0;
        for (int i = sortList.size() - 1; i >= 0; i--) {
            IdSummaryStatisticsMeta<PK, Integer, Long> meta = sortList.get(i);
            if (meta.getSum() < currentScore) {
                currentRanking++;
                currentScore = meta.getSum();
            }
            consumer.accept(IdRankingMeta.of(meta.getId(), currentRanking));
        }
    }
}
