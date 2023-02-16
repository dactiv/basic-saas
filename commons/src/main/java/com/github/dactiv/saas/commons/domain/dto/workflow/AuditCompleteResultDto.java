package com.github.dactiv.saas.commons.domain.dto.workflow;

import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 自定义申请 dto 审批结果， 用于创建自定义流程实现
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditCompleteResultDto extends AuditOperationDto {
    @Serial
    private static final long serialVersionUID = -2774926658741558614L;

    /**
     * 状态
     */
    private ApplyStatusEnum status;

}
