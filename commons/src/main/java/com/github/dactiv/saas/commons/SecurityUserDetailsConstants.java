package com.github.dactiv.saas.commons;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.domain.AnonymousUser;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 安全明细变量
 */
public interface SecurityUserDetailsConstants {

    String SECURITY_DETAILS_REAL_NAME_KEY = "realName";

    String SECURITY_DETAILS_USERNAME_KEY = "username";

    String SECURITY_DETAILS_EMAIL_KEY = "email";

    String SECURITY_DETAILS_PHONE_NUMBER_KEY = "phoneNumber";

    String SECURITY_DETAILS_ID_CARD_NUMBER_KEY = "idCardNumber";

    String SECURITY_DETAILS_INITIALIZATION_META_KEY = "initializationMeta";

    String SECURITY_DETAILS_DEPARTMENT_KEY = "departmentMetas";

    String SECURITY_DETAILS_GENDER_KEY = "gender";

    String SECURITY_DETAILS_WECHAT_META_KEY = "wechatMeta";

    String SECURITY_DETAILS_NEW_USER_KEY = "isNew";

    String USER_ID_TABLE_FIELD = "user_id";

    String USER_TYPE_TABLE_FIELD = "user_type";

    String DEFAULT_ANONYMOUS_NAME = "匿名用户";

    BasicUserDetails<Integer> DEFAULT_ANONYMOUS = BasicUserDetails.of(0, DEFAULT_ANONYMOUS_NAME, ResourceSourceEnum.SYSTEM_SOURCE_VALUE);

    static <T> BasicUserDetails<T> /**/toBasicUserDetails(SecurityUserDetails details) {
        return toBasicUserDetails(details, SECURITY_DETAILS_REAL_NAME_KEY);
    }

    static <T> BasicUserDetails<T> toBasicUserDetails(SecurityUserDetails details, String realNameKey) {

        BasicUserDetails<T> userDetails = details.toBasicUserDetails();

        if (details.getMeta().containsKey(realNameKey)) {
            String realName = details.getMeta().get(realNameKey).toString();

            if (StringUtils.isNotBlank(realName)) {
                userDetails.setUsername(realName);
            }
        }
        return userDetails;
    }

    static void convertAnonymousUser(AnonymousUser<Integer> userDetails) {
        convertAnonymousUser(userDetails, DEFAULT_ANONYMOUS_NAME);
    }

    static void convertAnonymousUser(AnonymousUser<Integer> userDetails, String anonymousName) {
        if (YesOrNo.No.equals(userDetails.getAnonymous())) {
            return;
        }
        userDetails.setUsername(anonymousName);
        userDetails.setUserType(ResourceSourceEnum.SYSTEM_SOURCE_VALUE);
        userDetails.setUserId(0);
    }

    static String getRealName(SecurityUserDetails details) {
        return getRealName(details, SECURITY_DETAILS_REAL_NAME_KEY);
    }

    static String getRealName(SecurityUserDetails details, String realNameKey) {

        String result = details.getUsername();
        if (details.getMeta().containsKey(realNameKey)) {
            String realName = details.getMeta().get(realNameKey).toString();

            if (StringUtils.isNotBlank(realName)) {
                result = realName;
            }
        }

        return result;
    }

    static String getRealName(Map<String, Object> user) {
        return getRealName(user, SecurityUserDetailsConstants.SECURITY_DETAILS_REAL_NAME_KEY);
    }

    static String getRealName(Map<String, Object> user, String realNameKey) {
        Object result = Objects.requireNonNull(
                user.getOrDefault(realNameKey, user.get(SecurityUserDetailsConstants.SECURITY_DETAILS_USERNAME_KEY)),
                "通过 [" + realNameKey + "] 和 [" + SecurityUserDetailsConstants.SECURITY_DETAILS_USERNAME_KEY + "] 在数据 【" + user + "】 找不到任何信息"
        );

        return result.toString();
    }

    static <T> void equals(TypeUserDetails<T> source, SecurityUserDetails target) {
        equals(source, target, "ID 为 [" + target.getId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void equals(TypeUserDetails<T> source, SecurityUserDetails target, String message) {
        contains(List.of(source), target, message);
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, SecurityUserDetails target) {
        contains(sources, target, "ID 为 [" + target.getId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, SecurityUserDetails target, String message) {
        Assert.isTrue(
                sources.stream().anyMatch(t -> t.getUserType().equals(target.getType()) && t.getUserId().equals(target.getId())),
                message
        );
    }

}
