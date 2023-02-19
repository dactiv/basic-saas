package com.github.dactiv.saas.commons.domain.dto.workflow;

import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.AuditOperationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

/**
 * 带 ID 和用户信息的 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditOperationDto extends IdEntity<Integer> {
    @Serial
    private static final long serialVersionUID = -623704537533656307L;

    /**
     * 申请 id
     */
    @NotNull
    private Integer applyId;

    /**
     * 操作时间
     */
    @NotNull
    private Date operationTime = new Date();

    /**
     * 操作类型
     */
    @NotNull
    private AuditOperationTypeEnum operationType;

    /**
     * 表单类型
     */
    @NotNull
    private ApplyFormTypeEnum formType;
}
