package com.github.dactiv.saas.authentication.domain.meta;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户初始化实体类
 *
 * @author maurice
 * @since 2020-04-13 10:14:45
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class MemberUserInitializationMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 1714564243745969863L;

    /**
     * 是否可更新密码：1.是、0.否
     */
    @NotNull
    private YesOrNo randomPassword = YesOrNo.Yes;

    /**
     * 是否可更新登录账户：1.是、0.否
     */
    @NotNull
    private YesOrNo randomUsername = YesOrNo.Yes;
}