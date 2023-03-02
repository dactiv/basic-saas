package com.github.dactiv.saas.workflow.domain.body.response;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.saas.commons.domain.dto.workflow.AuditCompleteResultDto;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.FormParticipantTypeEnum;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 流程申请响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApplyResponseBody extends ApplyEntity {

    @Serial
    private static final long serialVersionUID = 8962200120665299295L;

    /**
     * 审批人信息
     */
    private List<ApplyApprovalEntity> approvalList;

    /**
     * 抄送人信息
     */
    private List<ApplyCopyEntity> copyList;

    @Override
    public CreateCustomApplyDto toCustomApplyDto() {
        CreateCustomApplyDto result = super.toCustomApplyDto();
        result.getParticipantList().addAll(
                approvalList
                        .stream()
                        .map(a -> AuditParticipantMeta.of(a, a.getSort(), FormParticipantTypeEnum.APPROVER))
                        .toList()
        );
        result.getParticipantList().addAll(
                copyList
                        .stream()
                        .map(a -> AuditParticipantMeta.of(a, a.getSort(), FormParticipantTypeEnum.COPY))
                        .toList()
        );
        return result;
    }

    public AuditCompleteResultDto customApplyResultDto() {
        CreateCustomApplyDto dto = toCustomApplyDto();

        AuditCompleteResultDto resultDto = Casts.of(dto, AuditCompleteResultDto.class);

        resultDto.setStatus(getStatus());
        resultDto.setFormType(dto.getType());

        return resultDto;
    }
}
