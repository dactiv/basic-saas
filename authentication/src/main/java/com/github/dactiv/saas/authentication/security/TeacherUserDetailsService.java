package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.config.AccessTokenConfig;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.TeacherService;
import com.github.dactiv.saas.commons.config.SchoolProperties;
import com.github.dactiv.saas.commons.domain.meta.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.service.WechatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 教师用户明细服务实现
 *
 * @author maurice.chen
 */
@Component
public class TeacherUserDetailsService extends MobileUserDetailService {

    private final TeacherService teacherService;

    public TeacherUserDetailsService(SchoolProperties schoolProperties, ApplicationConfig applicationConfig, PasswordEncoder passwordEncoder, AuthorizationService authorizationService, AuthenticationProperties authenticationProperties, DeviceIdContextRepository deviceIdContextRepository, WechatService wechatService, AccessTokenConfig accessTokenConfig, TeacherService teacherService) {
        super(schoolProperties, applicationConfig, passwordEncoder, authorizationService, authenticationProperties, deviceIdContextRepository, wechatService, accessTokenConfig);
        this.teacherService = teacherService;
    }

    @Override
    public List<String> getMobileType() {
        return List.of(ResourceSourceEnum.MOBILE_TEACHER_SOURCE_VALUE);
    }

    @Override
    public List<String> getWechatType() {
        return List.of(ResourceSourceEnum.WECHAT_TEACHER_SOURCE_VALUE);
    }

    @Override
    protected SystemUserEntity getBasicAuthenticationSystemUser(RequestAuthenticationToken token) {
        return teacherService.getByUsername(token.getPrincipal().toString());
    }

    @Override
    protected SystemUserEntity getSchoolSourceTypeSystemUser(RequestAuthenticationToken token) {
        return teacherService.getByNumber(token.getPrincipal().toString());
    }

    @Override
    protected SystemUserEntity getWechatTypeSystemUser(RequestAuthenticationToken token) {
        WechatAuthenticationToken wechatAuthenticationToken = Casts.cast(token);
        return teacherService.getByWechatAuthenticationToken(wechatAuthenticationToken);
    }

    @Override
    public List<String> getType() {
        return List.of(ResourceSourceEnum.MOBILE_TEACHER_SOURCE_VALUE, ResourceSourceEnum.TEACHER_SOURCE_VALUE, ResourceSourceEnum.WECHAT_TEACHER_SOURCE_VALUE);
    }

    @Override
    public SystemUserEntity convertTargetUser(SecurityUserDetails userDetails) {
        return teacherService.get(Casts.cast(userDetails.getId(), Integer.class));
    }

    @Override
    public SystemUserEntity convertTargetUser(TypeUserDetails<?> userDetails) {
        return teacherService.get(Casts.cast(userDetails.getUserId(), Integer.class));
    }

    @Override
    public void updatePassword(SystemUserEntity consoleUser, String oldPassword, String newPassword) {

        if (!getPasswordEncoder().matches(oldPassword, consoleUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        TeacherEntity user = consoleUser.ofIdData();
        user.setPassword(getPasswordEncoder().encode(newPassword));
        teacherService.updateById(user);
    }

    @Override
    public void onSuccessAuthentication(PrincipalAuthenticationToken result, HttpServletRequest request, HttpServletResponse response) {

        if (WechatAuthenticationToken.class.isAssignableFrom(result.getClass())) {
            WechatAuthenticationToken token = Casts.cast(result);
            TeacherEntity entity = Casts.cast(token.getDetails());

            if (!entity.getSessionKey().equals(token.getUserDetails().getSessionKey())) {
                entity.setSessionKey(token.getUserDetails().getSessionKey());
                teacherService.updateById(entity);
            }
        }

    }

    @Override
    protected void buildWechatUserDetailsMeta(SimpleWechatUserDetailsMeta meta, PrincipalAuthenticationToken result) {
        if (!SecurityUserDetails.class.isAssignableFrom(result.getDetails().getClass())) {
            return;
        }
        SecurityUserDetails userDetails = Casts.cast(result.getDetails());
        TeacherEntity entity = teacherService.get(Casts.cast(userDetails.getId(), Integer.class));
        teacherService.buildWechatUserDetails(meta, entity);
    }

    @Override
    protected void updateWechatSessionKey(SecurityUserDetails userDetails, WechatAuthenticationToken token) {
        TeacherEntity entity = teacherService.get(Casts.cast(userDetails.getId(), Integer.class));
        if (StringUtils.isEmpty(entity.getOpenId())) {
            entity.setOpenId(token.getUserDetails().getOpenId());
        }
        teacherService.updateWechatSessionKey(entity, token.getUserDetails().getSessionKey());
    }
}
