package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.security.ConsoleUserDetailsService;
import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

/**
 * <p>会员用户实体类</p>
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("member_user")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_member_user", autoResultMap = true)
public class MemberUserEntity extends SystemUserEntity implements PhoneNumberUserDetails, WechatUserDetails {

    /**
     * 联系电话
     */
    @NotNull
    @Length(max = 32)
    @Pattern(regexp = ConsoleUserDetailsService.IS_MOBILE_PATTERN_STRING)
    private String phoneNumber;

    /**
     * 身份证号码
     */
    private String idCardNumber;

    /**
     * session key
     */
    private String sessionKey;

    /**
     * 用户唯一标识
     */
    private String openId;

    /**
     * 用户在开放平台的唯一标识符
     */
    private String unionId;
}
