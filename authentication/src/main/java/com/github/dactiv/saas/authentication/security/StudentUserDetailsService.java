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
import com.github.dactiv.saas.authentication.domain.entity.StudentEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.StudentService;
import com.github.dactiv.saas.commons.config.SchoolProperties;
import com.github.dactiv.saas.commons.domain.meta.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.service.WechatService;
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
public class StudentUserDetailsService extends MobileUserDetailService {

    private final StudentService studentService;

    public StudentUserDetailsService(SchoolProperties schoolProperties,
                                     ApplicationConfig applicationConfig,
                                     PasswordEncoder passwordEncoder,
                                     AuthorizationService authorizationService,
                                     AuthenticationProperties authenticationProperties,
                                     DeviceIdContextRepository deviceIdContextRepository,
                                     WechatService wechatService,
                                     AccessTokenConfig accessTokenConfig,
                                     StudentService studentService) {
        super(schoolProperties, applicationConfig, passwordEncoder, authorizationService, authenticationProperties, deviceIdContextRepository, wechatService, accessTokenConfig);
        this.studentService = studentService;
    }

    @Override
    public List<String> getMobileType() {
        return List.of(ResourceSourceEnum.MOBILE_STUDENT_SOURCE_VALUE);
    }

    @Override
    public List<String> getWechatType() {
        return List.of(ResourceSourceEnum.WECHAT_STUDENT_SOURCE_VALUE);
    }

    @Override
    protected void buildWechatUserDetailsMeta(SimpleWechatUserDetailsMeta meta, PrincipalAuthenticationToken result) {
        if (!SecurityUserDetails.class.isAssignableFrom(result.getDetails().getClass())) {
            return;
        }
        SecurityUserDetails userDetails = Casts.cast(result.getDetails());
        StudentEntity entity = studentService.get(Casts.cast(userDetails.getId(), Integer.class));
        studentService.buildWechatUserDetails(meta, entity);
    }

    @Override
    protected void updateWechatSessionKey(SecurityUserDetails userDetails, WechatAuthenticationToken token) {
        StudentEntity entity = studentService.get(Casts.cast(userDetails.getId(), Integer.class));
        if (StringUtils.isEmpty(entity.getOpenId())) {
            entity.setOpenId(token.getUserDetails().getOpenId());
        }
        studentService.updateWechatSessionKey(entity, token.getUserDetails().getSessionKey());
    }

    @Override
    protected SystemUserEntity getBasicAuthenticationSystemUser(RequestAuthenticationToken token) {
        return studentService.getByUsername(token.getPrincipal().toString());
    }

    @Override
    protected SystemUserEntity getSchoolSourceTypeSystemUser(RequestAuthenticationToken token) {
        return studentService.getByUsername(token.getPrincipal().toString());
    }

    @Override
    protected SystemUserEntity getWechatTypeSystemUser(RequestAuthenticationToken token) {
        WechatAuthenticationToken wechatAuthenticationToken = Casts.cast(token);
        return studentService.getByWechatAuthenticationToken(wechatAuthenticationToken);
    }

    @Override
    public List<String> getType() {
        return ResourceSourceEnum.STUDENT.getValue();
    }

    @Override
    public SystemUserEntity convertTargetUser(SecurityUserDetails userDetails) {
        return studentService.get(Casts.cast(userDetails.getId(), Integer.class));
    }

    @Override
    public SystemUserEntity convertTargetUser(TypeUserDetails<?> userDetails) {
        return studentService.get(Casts.cast(userDetails.getUserId(), Integer.class));
    }

    @Override
    public void updatePassword(SystemUserEntity consoleUser, String oldPassword, String newPassword) {

        if (!getPasswordEncoder().matches(oldPassword, consoleUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        StudentEntity user = consoleUser.ofIdData();
        user.setPassword(getPasswordEncoder().encode(newPassword));
        studentService.updateById(user);
    }
}
