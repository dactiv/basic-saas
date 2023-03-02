package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.config.MemberUserRegisterConfig;
import com.github.dactiv.saas.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.domain.meta.MemberUserInitializationMeta;
import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.MemberUserService;
import com.github.dactiv.saas.commons.domain.meta.wechat.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.service.WechatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Objects;

/**
 * 会员用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
public class MemberUserDetailsService extends MobileUserDetailService {

    private final MemberUserService memberUserService;

    private final MemberUserRegisterConfig memberUserRegisterConfig;

    public MemberUserDetailsService(ApplicationConfig applicationConfig,
                                    MemberUserRegisterConfig memberUserRegisterConfig,
                                    PasswordEncoder passwordEncoder,
                                    AuthorizationService authorizationService,
                                    AuthenticationProperties authenticationProperties,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    WechatService wechatService,
                                    MemberUserService memberUserService) {
        super(
                applicationConfig,
                passwordEncoder,
                authorizationService,
                authenticationProperties,
                deviceIdContextRepository,
                wechatService
        );

        this.memberUserRegisterConfig = memberUserRegisterConfig;
        this.memberUserService = memberUserService;
    }

    @Override
    public List<String> getMobileType() {
        return List.of(ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE);
    }

    @Override
    public List<String> getWechatType() {
        return List.of(ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE);
    }

    @Override
    protected void buildWechatUserDetailsMeta(SimpleWechatUserDetailsMeta meta, PrincipalAuthenticationToken result) {

        if (!SecurityUserDetails.class.isAssignableFrom(result.getDetails().getClass())) {
            return ;
        }
        SecurityUserDetails userDetails = Casts.cast(result.getDetails());
        MemberUserEntity entity = memberUserService.get(Casts.cast(userDetails.getId(), Integer.class));
        memberUserService.buildWechatUserDetails(meta, entity);
    }

    @Override
    protected void updateWechatSessionKey(SecurityUserDetails userDetails, WechatAuthenticationToken token) {
        MemberUserEntity entity = memberUserService.get(Casts.cast(userDetails.getId(), Integer.class));
        if (StringUtils.isEmpty(entity.getOpenId())) {
            entity.setOpenId(token.getUserDetails().getOpenId());
        }
        memberUserService.updateWechatSessionKey(entity, token.getUserDetails().getSessionKey());
    }

    @Override
    protected SystemUserEntity getBasicAuthenticationSystemUser(RequestAuthenticationToken token) {
        MemberUserEntity user = memberUserService.getByUsername(token.getPrincipal().toString());

        if (Objects.isNull(user)) {
            user = createMemberUser(token.getPrincipal().toString());
        }

        return user;
    }

    private MemberUserEntity createMemberUser(String phoneNumber) {

        MemberUserEntity user = new MemberUserEntity();

        user.setUsername(RandomStringUtils.randomAlphanumeric(memberUserRegisterConfig.getRandomUsernameCount()) + phoneNumber);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(generateRandomPassword());
        user.setStatus(UserStatus.Enabled);
        user.setInitializationMeta(new MemberUserInitializationMeta());

        memberUserService.insert(user);

        return user;
    }

    private String generateRandomPassword() {
        String key = RandomStringUtils.randomAlphanumeric(memberUserRegisterConfig.getRandomUsernameCount()) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    @Override
    protected SystemUserEntity getWechatTypeSystemUser(RequestAuthenticationToken token) {
        WechatAuthenticationToken wechatAuthenticationToken = Casts.cast(token);
        MemberUserEntity user = memberUserService.getByWechatAuthenticationToken(wechatAuthenticationToken);
        if (Objects.isNull(user)) {
            user = createMemberUser(wechatAuthenticationToken.getPrincipal().toString());
        }

        return user;
    }

    @Override
    public SystemUserEntity convertTargetUser(SecurityUserDetails userDetails) {
        return memberUserService.get(Casts.cast(userDetails.getId(), Integer.class));
    }

    @Override
    public SystemUserEntity convertTargetUser(TypeUserDetails<?> userDetails) {
        return memberUserService.get(Casts.cast(userDetails.getUserId(), Integer.class));
    }

    @Override
    public void updatePassword(SystemUserEntity systemUser, String oldPassword, String newPassword) {

        if (!getPasswordEncoder().matches(oldPassword, systemUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        MemberUserEntity user = systemUser.ofIdData();
        user.setPassword(getPasswordEncoder().encode(newPassword));
        memberUserService.updateById(user);
    }

    @Override
    public void onSuccessAuthentication(PrincipalAuthenticationToken result, HttpServletRequest request, HttpServletResponse response) {

        if (!WechatAuthenticationToken.class.isAssignableFrom(result.getClass())) {
            return ;
        }
        WechatAuthenticationToken token = Casts.cast(result);
        MemberUserEntity entity = Casts.cast(token.getDetails());

        if (entity.getSessionKey().equals(token.getUserDetails().getSessionKey())) {
            return ;
        }

        entity.setSessionKey(token.getUserDetails().getSessionKey());
        memberUserService.updateById(entity);
    }
}
