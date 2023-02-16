package com.github.dactiv.saas.commons.domain.meta.workflow;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 工作流审核元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditMeta extends BasicUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = 7546942387331286334L;

    /**
     * 表单名称
     */
    @NotNull
    private String formName;

    /**
     * 审核类型
     */
    @NotNull
    private FormApprovalTypeEnum approvalType;

    /**
     * 参与者信息
     */
    @NotEmpty
    private List<AuditParticipantMeta> participantList = new LinkedList<>();
}
