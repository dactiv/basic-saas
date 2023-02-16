package com.github.dactiv.saas.commons.domain.dto.workflow;

import com.github.dactiv.framework.security.entity.TypeUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAuditCompleteResultDto extends AuditCompleteResultDto implements TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = -1275609554597760258L;

    /**
     * 用户 id
     */
    private Integer userId;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户类型
     */
    private String userType;
}
