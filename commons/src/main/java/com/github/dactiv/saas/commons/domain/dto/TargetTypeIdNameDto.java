package com.github.dactiv.saas.commons.domain.dto;

import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 带目标 id 的类型 id 名称元数据 dto
 *
 * @author maurice.chen
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class TargetTypeIdNameDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 2720669946368645240L;
    /**
     * 目标 id
     */
    private Integer targetId;

    /**
     * 类型 id 名称元数据集合
     */
    private List<TypeIdNameMeta> typeIdNameMetas;
}
