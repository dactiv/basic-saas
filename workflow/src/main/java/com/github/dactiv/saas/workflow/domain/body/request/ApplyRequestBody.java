package com.github.dactiv.saas.workflow.domain.body.request;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程申请请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApplyRequestBody extends ApplyEntity {

    @Serial
    private static final long serialVersionUID = -365528030640081956L;

    /**
     * 审核人信息
     */
    @NotEmpty
    private List<AuditParticipantMeta> participantList;

    public static ApplyRequestBody of(CreateCustomApplyDto dto) {
        ApplyRequestBody body = new ApplyRequestBody();

        body.setUserDetails(dto);

        body.setApprovalType(dto.getApprovalType());
        body.setApplyContent(dto.getApplyMeta());

        body.setFormId(dto.getId());
        body.setFormName(dto.getFormName());
        body.setFormType(dto.getType());
        body.setFormContent(dto.getContentMeta());

        body.setParticipantList(dto.getParticipantList().stream().map(p -> Casts.of(p, AuditParticipantMeta.class)).collect(Collectors.toList()));

        return body;
    }

}
