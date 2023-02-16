package com.github.dactiv.saas.authentication.domain.meta;

import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 邀请老师请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InviteTeacherMeta extends InviteUserMeta {

    @Serial
    private static final long serialVersionUID = -3274912526362206974L;

    /**
     * 科目信息
     */
    private List<IdNameMeta> subjectsInfo;
}
