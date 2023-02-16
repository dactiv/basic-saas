package com.github.dactiv.saas.commons.domain.dto.workflow;

import com.github.dactiv.framework.security.entity.TypeUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 带用户审核的操作数据 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAuditOperationDto extends AuditOperationDto implements TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = -2002262016034469623L;

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
