package com.github.dactiv.saas.commons.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 范围梯队统计 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class LadderRangeStatisticsDto extends LadderStatisticsDto {

    @Serial
    private static final long serialVersionUID = -5130911913276131799L;
    /**
     * 最低分
     */
    private int min;
}
