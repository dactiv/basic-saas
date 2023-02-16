package com.github.dactiv.saas.commons.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 带子节点的雷达统计 dto
 *
 * @param <T> 总分类型
 * @param <T>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChildrenRadarStatisticsDto<T extends Number> extends RadarStatisticsDto<T> {
    @Serial
    private static final long serialVersionUID = -3272673622464790373L;
    /**
     * 子项目
     */
    private List<ChildrenRadarStatisticsDto<T>> children = new LinkedList<>();
}
