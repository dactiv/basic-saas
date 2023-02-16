package com.github.dactiv.saas.commons.domain.dto;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 带 id 值的基础用户名称
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdValueBasicUserDetails extends BasicUserDetails<Integer> {
    @Serial
    private static final long serialVersionUID = -646958379455328131L;

    private Integer id;
}
