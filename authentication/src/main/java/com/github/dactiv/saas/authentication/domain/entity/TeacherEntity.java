package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeView;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.security.ConsoleUserDetailsService;
import com.github.dactiv.saas.authentication.security.UserDetailsAccessToken;
import com.github.dactiv.saas.authentication.domain.meta.ClassGradesMeta;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * <p>Table: tb_teacher - 教师表</p>
 *
 * @author maurice.chen
 * @since 2022-03-07 11:19:27
 */
@Data
@NoArgsConstructor
@Alias("teacher")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_teacher", autoResultMap = true)
@IncludeView(
        value = IncludeView.ID_NAME_VIEW,
        properties = {
                IdEntity.ID_FIELD_NAME,
                SecurityUserDetailsConstants.SECURITY_DETAILS_USERNAME_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_EMAIL_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_PHONE_NUMBER_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_NUMBER_KEY,
                SecurityUserDetailsConstants.SECURITY_DETAILS_REAL_NAME_KEY
        }
)
public class TeacherEntity extends SystemUserEntity implements UserDetailsAccessToken, PhoneNumberUserDetails, WechatUserDetails {

    @Serial
    private static final long serialVersionUID = 1461345084274196919L;

    public final static List<String> copyProperties = List.of(
            "username",
            "email",
            "password",
            "gender",
            "realName",
            "number",
            "phoneNumber"
    );

    /**
     * 真实姓名
     */
    @NotNull
    @Length(max = 16)
    private String realName;

    /**
     * 电话号码
     */
    @Length(max = 32)
    @Pattern(regexp = ConsoleUserDetailsService.IS_MOBILE_PATTERN_STRING)
    private String phoneNumber;

    /**
     * 工号
     */
    private String number;

    /**
     * 部门信息
     */
    @JsonCollectionGenericType(IdNameMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdNameMeta> departmentsInfo;

    /**
     * 所属科目
     */
    @JsonCollectionGenericType(IdNameMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdNameMeta> subjectsInfo;

    /**
     * 所带班级
     */
    @JsonCollectionGenericType(ClassGradesMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ClassGradesMeta> classGradesInfo;

    /**
     * 目标 id
     */
    private String targetId;

    /**
     * 访问 token
     */
    private String accessToken;

    /**
     * 教师资格证号
     */
    private String qualificationCertificateNo;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 是否可发布资源系统资源
     */
    private YesOrNo publishResource = YesOrNo.No;

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

        if (StringUtils.isNotBlank(number)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_NUMBER_KEY, number);
        }

        if (CollectionUtils.isNotEmpty(departmentsInfo)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_DEPARTMENT_KEY, Casts.convertValue(departmentsInfo, List.class));
        }

        if (CollectionUtils.isNotEmpty(subjectsInfo)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_SUBJECT_KEY, Casts.convertValue(subjectsInfo, List.class));
        }

        if (CollectionUtils.isNotEmpty(classGradesInfo)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_CLASS_GRADES_INFO_KEY, Casts.convertValue(classGradesInfo, List.class));
        }

        if (Objects.nonNull(publishResource)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_RESOURCE_PUBLISH_RESOURCE_KEY, publishResource);
        }

        return result;
    }
}