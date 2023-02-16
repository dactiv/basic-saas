package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeView;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.security.UserDetailsAccessToken;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_student - 学生表</p>
 *
 * @author maurice.chen
 * @since 2022-02-10 05:54:36
 */
@Data
@NoArgsConstructor
@Alias("student")
@EqualsAndHashCode(callSuper = true)
@IncludeView(
        value = IncludeView.ID_NAME_VIEW,
        properties = {
                IdEntity.ID_FIELD_NAME,
                SecurityUserDetailsConstants.SECURITY_DETAILS_USERNAME_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_EMAIL_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_REAL_NAME_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_STUDENT_CODE_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_NUMBER_KEY
        }
)
@TableName(value = "tb_student", autoResultMap = true)
public class StudentEntity extends SystemUserEntity implements UserDetailsAccessToken, PhoneNumberUserDetails, WechatUserDetails {

    @Serial
    private static final long serialVersionUID = -8831919622866464267L;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话号码
     */
    private String phoneNumber;

    /**
     * 学号
     */
    private String number;

    /**
     * 学籍号
     */
    private String code;

    /**
     * 目标 id
     */
    private String targetId;

    /**
     * 访问 token
     */
    private String accessToken;

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

        if (StringUtils.isNotBlank(code)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_STUDENT_CODE_KEY, code);
        }

        if (StringUtils.isNotBlank(number)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_NUMBER_KEY, number);
        }

        if (CollectionUtils.isNotEmpty(departmentsInfo)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_DEPARTMENT_KEY, Casts.convertValue(departmentsInfo, List.class));
        }
        return result;
    }
}