package com.github.dactiv.saas.commons.domain.dto.workflow;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义申请 dto， 用于创建自定义流程实现
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateCustomApplyDto extends AuditMeta implements BasicIdentification<Integer> {

    @Serial
    private static final long serialVersionUID = -1082489820611055718L;

    /**
     * 主键 id
     */
    @NotNull
    private Integer id;

    /**
     * 类型
     */
    @NotNull
    private ApplyFormTypeEnum type;

    /**
     * 内容元数据信息
     */
    private Map<String, Object> contentMeta = new LinkedHashMap<>();

    /**
     * 申请元数据信息
     */
    @NotEmpty
    private Map<String, Object> applyMeta = new LinkedHashMap<>();
}
