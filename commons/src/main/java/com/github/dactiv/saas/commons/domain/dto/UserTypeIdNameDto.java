package com.github.dactiv.saas.commons.domain.dto;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class UserTypeIdNameDto<T> extends TypeIdNameMeta {
    @Serial
    private static final long serialVersionUID = 2433311709052102280L;

    /**
     * 用户明细
     */
    private BasicUserDetails<T> userDetails;
}
