package com.github.dactiv.saas.commons.domain.entity;

import com.github.dactiv.saas.commons.domain.meta.BasicTrendMeta;
import com.github.dactiv.saas.commons.enumeration.StatisticsCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 在线学习趋势图实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class TrendDataEntity extends BasicTrendMeta {

    @Serial
    private static final long serialVersionUID = 6847378283078451879L;

    public static final String DEFAULT_INDEX_NAME = "t_data_trend";

    public static final String CATEGORY_FIELD_NAME = "category";

    /**
     * 数量
     */
    private Long count = 0L;

    /**
     * 类别
     */
    private StatisticsCategoryEnum category;

    public static TrendDataEntity of(BasicTrendMeta meta, LocalDateTime now, StatisticsCategoryEnum category) {
        TrendDataEntity trendEntity = new TrendDataEntity();

        trendEntity.setCreationTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        trendEntity.setTargetId(meta.getTargetId());
        trendEntity.setTargetType(meta.getTargetType());
        trendEntity.setMeta(meta.getMeta());
        trendEntity.setMetaUniqueValue(meta.getMetaUniqueValue());
        trendEntity.setCategory(category);

        return trendEntity;
    }
}
