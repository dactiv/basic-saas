package com.github.dactiv.saas.authentication.domain.body.request;

import com.github.dactiv.saas.authentication.domain.meta.InviteUserMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户邀请请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class UserInviteRequestBody<T extends InviteUserMeta> implements Serializable {


    @Serial
    private static final long serialVersionUID = 1321796557088568775L;

    /**
     * 邀请开始时间
     */
    private Date startTime;

    /**
     * 邀请结束时间
     */
    private Date endTime;

    /**
     * 受邀人集合
     */
    private List<T> invitees;
}
