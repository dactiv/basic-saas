package com.github.dactiv.saas.commons.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 梯队统计 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class LadderStatisticsDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5273774878610640439L;

    /**
     * 最高分
     */
    private int max;
    /**
     * 顺序值
     */
    private int sort;
    /**
     * 人数
     */
    private int count;
}
