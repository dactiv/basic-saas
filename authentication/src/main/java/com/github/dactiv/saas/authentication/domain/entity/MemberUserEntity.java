package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.domain.meta.MemberUserInitializationMeta;
import com.github.dactiv.saas.authentication.security.MobileUserDetailService;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.meta.wechat.SimpleWechatUserDetailsMeta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.util.Map;
import java.util.Objects;

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
public class MemberUserEntity extends SystemUserEntity implements PhoneNumberUserDetails {

    /**
     * 联系电话
     */
    @NotNull
    @Length(max = 32)
    @Pattern(regexp = MobileUserDetailService.IS_MOBILE_PATTERN_STRING)
    private String phoneNumber;

    /**
     * 身份证号码
     */
    private String idCardNumber;

    /**
     * 微信信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private SimpleWechatUserDetailsMeta wechatMeta;

    /**
     * 用户初始化
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MemberUserInitializationMeta initializationMeta;

    /**
     * 转换 security user 明细元数据
     *
     * @return map
     */
    public Map<String, Object> toSecurityUserDetailsMeta() {

        Map<String, Object> result = super.toSecurityUserDetailsMeta();

        if (StringUtils.isNotBlank(phoneNumber)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY, phoneNumber);
        }

        if (Objects.nonNull(idCardNumber)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_ID_CARD_NUMBER_KEY, idCardNumber);
        }

        if (Objects.nonNull(initializationMeta)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_INITIALIZATION_META_KEY, initializationMeta);
        }

        if (Objects.nonNull(wechatMeta)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_INITIALIZATION_META_KEY, initializationMeta);
        }

        return result;
    }

    public static MemberUserEntity of(SecurityUserDetails mobileUserDetails) {
        MemberUserEntity memberUserEntity = new MemberUserEntity();

        memberUserEntity.setUsername(mobileUserDetails.getUsername());
        memberUserEntity.setPassword(mobileUserDetails.getPassword());
        memberUserEntity.setStatus(mobileUserDetails.getStatus());

        if (mobileUserDetails.getMeta().containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY)) {
            String phoneNumber = mobileUserDetails.getMeta().get(SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY).toString();
            memberUserEntity.setPhoneNumber(phoneNumber);
        }

        if (mobileUserDetails.getMeta().containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_ID_CARD_NUMBER_KEY)) {
            String idCardNumber = mobileUserDetails.getMeta().get(SecurityUserDetailsConstants.SECURITY_DETAILS_ID_CARD_NUMBER_KEY).toString();
            memberUserEntity.setIdCardNumber(idCardNumber);
        }

        if (mobileUserDetails.getMeta().containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_INITIALIZATION_META_KEY)) {
            MemberUserInitializationMeta meta = Casts.convertValue(mobileUserDetails.getMeta().get(SecurityUserDetailsConstants.SECURITY_DETAILS_INITIALIZATION_META_KEY), MemberUserInitializationMeta.class);
            memberUserEntity.setInitializationMeta(meta);
        }

        if (mobileUserDetails.getMeta().containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_INITIALIZATION_META_KEY)) {
            SimpleWechatUserDetailsMeta meta = Casts.convertValue(mobileUserDetails.getMeta().get(SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY),SimpleWechatUserDetailsMeta.class);
            memberUserEntity.setWechatMeta(meta);
        }

        return memberUserEntity;
    }
}
