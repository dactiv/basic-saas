package com.github.dactiv.saas.commons.domain.meta.workflow;

import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 表单元数据，用于继承父累使用
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormMeta extends IntegerIdEntity {

    @Serial
    private static final long serialVersionUID = -570925744954671996L;

    /**
     * 表单名称
     */
    private String name;
}
