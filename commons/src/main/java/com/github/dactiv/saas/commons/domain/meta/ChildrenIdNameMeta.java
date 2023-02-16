package com.github.dactiv.saas.commons.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 带子节点的 id 名称实体
 *
 * @param <T> 子节点集合类型
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "children")
public class ChildrenIdNameMeta<T> extends TypeIdNameMeta {

    @Serial
    private static final long serialVersionUID = -5204582208323566939L;

    /**
     * 子节点
     */
    private List<T> children = new LinkedList<>();
}
