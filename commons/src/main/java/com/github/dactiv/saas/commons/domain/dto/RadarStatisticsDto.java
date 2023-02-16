package com.github.dactiv.saas.commons.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 雷达统计 dto
 *
 * @param <T> 总分类型
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class RadarStatisticsDto<T extends Number> implements Serializable {

    @Serial
    private static final long serialVersionUID = -2862080276293927186L;
    /**
     * 名称
     */
    private String name;

    /**
     * 占比
     */
    private Double proportion = 0.00;

    /**
     * 总分
     */
    private T totalScore;


}
