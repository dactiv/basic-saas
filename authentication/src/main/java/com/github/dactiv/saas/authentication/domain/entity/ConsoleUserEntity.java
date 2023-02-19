package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.security.ConsoleUserDetailsService;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * <p>系统用户实体类</p>
 * <p>Table: tb_console_user - 系统用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@NoArgsConstructor
@Alias("consoleUser")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_console_user", autoResultMap = true)
public class ConsoleUserEntity extends SystemUserEntity implements PhoneNumberUserDetails {

    public static final String IS_MOBILE_PATTERN_STRING = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";

    @Serial
    private static final long serialVersionUID = 542256170672538050L;

    /**
     * 真实姓名
     */
    @NotEmpty
    @Length(max = 16)
    private String realName;

    /**
     * 联系电话
     */
    @Length(max = 32)
    @Pattern(regexp = ConsoleUserDetailsService.IS_MOBILE_PATTERN_STRING)
    private String phoneNumber;

    /**
     * 部门信息
     */
    @JsonCollectionGenericType(IdNameMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdNameMeta> departmentsInfo;

    /**
     * 备注
     */
    private String remark;

    @Override
    public Map<String, Object> toSecurityUserDetailsMeta() {
        Map<String, Object> result = super.toSecurityUserDetailsMeta();

        if (StringUtils.isNotBlank(phoneNumber)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY, phoneNumber);
        }

        if (StringUtils.isNotBlank(realName)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_REAL_NAME_KEY, realName);
        }
        return result;
    }
}