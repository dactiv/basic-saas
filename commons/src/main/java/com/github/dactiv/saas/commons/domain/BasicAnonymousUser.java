package com.github.dactiv.saas.commons.domain;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serial;

/**
 * 匿名用户
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicAnonymousUser<T> extends BasicUserDetails<T> implements AnonymousUser<T> {

    @Serial
    private static final long serialVersionUID = 1410417307992198636L;
    /**
     * 是否匿名
     */
    @NotNull
    private YesOrNo anonymous = YesOrNo.No;
}
