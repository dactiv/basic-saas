package com.github.dactiv.saas.workflow.domain.body;

import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 流程表单请求响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormBody extends FormEntity {

    @Serial
    private static final long serialVersionUID = 1167696214225786711L;

    /**
     * 参与者集合
     */
    private List<AuditParticipantMeta> participantList;
}
