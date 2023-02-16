package com.github.dactiv.saas.commons.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分数等级范围配置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
public class ScoreLevelRangeProperties {

    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;

    /**
     * 最小值
     */
    private Double min;

    /**
     * 最大值
     */
    private Double max;

    public Double getAverage() {
        return min + ((max - min) / 2);
    }

}
