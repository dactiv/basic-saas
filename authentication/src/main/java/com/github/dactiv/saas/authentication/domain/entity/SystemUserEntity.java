package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.meta.IdRoleAuthorityMeta;
import com.github.dactiv.saas.commons.enumeration.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.*;

/**
 * 用户实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemUserEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 750742816513263456L;
    /**
     * 密码字段名称
     */
    public static final String PASSWORD_FIELD_NAME = "password";

    /**
     * 邮箱
     */
    @Email
    @Length(max = 64)
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @NotNull
    private UserStatus status;

    /**
     * 性别
     */
    private GenderEnum gender;

    /**
     * 登录帐号
     */
    @NotEmpty
    @Length(max = 12)
    private String username;

    /**
     * 所属组集合
     */
    @JsonCollectionGenericType(IdRoleAuthorityMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdRoleAuthorityMeta> groupsInfo = new LinkedList<>();

    /**
     * 独立权限资源 id 集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, List<String>> resourceMap = new LinkedHashMap<>();

    /**
     * 转换 security user 明细元数据
     *
     * @return map
     */
    public Map<String, Object> toSecurityUserDetailsMeta() {

        Map<String, Object> result = new LinkedHashMap<>();

        if (StringUtils.isNotBlank(email)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_EMAIL_KEY, email);
        }

        if (Objects.nonNull(gender)) {
            result.put(SecurityUserDetailsConstants.SECURITY_DETAILS_GENDER_KEY, Casts.convertValue(gender, Map.class));
        }

        return result;
    }

}
