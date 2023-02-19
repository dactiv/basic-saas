package com.github.dactiv.saas.commons.domain.meta.workflow;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.FormParticipantTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 表单参与者信息
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditParticipantMeta extends BasicUserDetails<Integer> {
    @Serial
    private static final long serialVersionUID = 4236866574466057277L;

    /**
     * 顺序值
     */
    @NotNull
    private Integer sort;

    /**
     * 参数者类型
     */
    @NotNull
    private FormParticipantTypeEnum type;

    public static AuditParticipantMeta of(BasicUserDetails<Integer> userDetails, Integer sort, FormParticipantTypeEnum type) {
        AuditParticipantMeta result = new AuditParticipantMeta();
        result.setUserDetails(userDetails);
        result.setSort(sort);
        result.setType(type);
        return result;
    }
}
