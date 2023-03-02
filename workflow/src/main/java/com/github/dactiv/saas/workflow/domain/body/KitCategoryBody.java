package com.github.dactiv.saas.workflow.domain.body;

import com.github.dactiv.saas.workflow.domain.entity.KitCategoryEntity;
import com.github.dactiv.saas.workflow.domain.entity.KitEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 配件类别请求响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KitCategoryBody extends KitCategoryEntity {

    @Serial
    private static final long serialVersionUID = 9023722486482939685L;

    /**
     * 配件实体集合
     */
    private List<KitEntity> item = new LinkedList<>();
}
