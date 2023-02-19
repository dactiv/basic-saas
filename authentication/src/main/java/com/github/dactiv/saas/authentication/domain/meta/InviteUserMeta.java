package com.github.dactiv.saas.authentication.domain.meta;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

/**
 * 邀请教师元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class InviteUserMeta implements BasicIdentification<Integer> {

    @Serial
    private static final long serialVersionUID = 5467679463056216486L;

    /**
     * 主键 id
     */
    @NotNull
    private Integer id;

    /**
     * 登陆账号
     */
    private String username;

    /**
     * 电话号码
     */
    private String phoneNumber;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 受邀时间
     */
    private Date invitedTime;
}
