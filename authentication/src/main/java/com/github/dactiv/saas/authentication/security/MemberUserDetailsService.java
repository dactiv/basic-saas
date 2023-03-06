package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
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
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.MemberUserService;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 会员用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
public class MemberUserDetailsService extends MobileUserDetailService {

    private final MemberUserService memberUserService;

    private final MemberUserRegisterConfig memberUserRegisterConfig;

    private final ConfigServiceFeignClient configServiceFeignClient;

    public MemberUserDetailsService(ApplicationConfig applicationConfig,
                                    MemberUserRegisterConfig memberUserRegisterConfig,
                                    PasswordEncoder passwordEncoder,
                                    AuthorizationService authorizationService,
                                    AuthenticationProperties authenticationProperties,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    MemberUserService memberUserService,
                                    ConfigServiceFeignClient configServiceFeignClient) {
        super(
                applicationConfig,
                passwordEncoder,
                authorizationService,
                authenticationProperties,
                deviceIdContextRepository
        );

        this.memberUserRegisterConfig = memberUserRegisterConfig;
        this.memberUserService = memberUserService;
        this.configServiceFeignClient = configServiceFeignClient;
    }

    @Override
    public List<String> getMobileType() {
        return List.of(ResourceSourceEnum.APP_MEMBER_SOURCE_VALUE);
    }

    @Override
    protected SystemUserEntity getWakeUpTypeSystemUser(RequestAuthenticationToken token) {
        return memberUserService.getByUsername(token.getPrincipal().toString());
    }

    @Override
    protected SystemUserEntity getMobileTypeSystemUser(RequestAuthenticationToken token) {
        SystemUserEntity result = memberUserService.getByUsername(token.getPrincipal().toString());

        if (Objects.isNull(result)) {
            String phone = token.getPrincipal().toString();

            if (!StringUtils.isNumeric(phone) || !Pattern.compile(IS_MOBILE_PATTERN_STRING).matcher(phone).matches()) {
                throw new BadCredentialsException("手机号码不正确");
            }
            result = createMemberUser(phone);
        }

        return result;
    }

    private MemberUserEntity createMemberUser(String phoneNumber) {

        MemberUserEntity user = new MemberUserEntity();

        user.setUsername(RandomStringUtils.randomAlphanumeric(memberUserRegisterConfig.getRandomUsernameCount()) + phoneNumber);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(generateRandomPassword());
        user.setStatus(UserStatus.Enabled);
        user.setInitializationMeta(new MemberUserInitializationMeta());

        return user;
    }

    private String generateRandomPassword() {
        String key = RandomStringUtils.randomAlphanumeric(memberUserRegisterConfig.getRandomUsernameCount()) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
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
    public boolean matchesPassword(String presentedPassword, RequestAuthenticationToken token, SecurityUserDetails userDetails) {
        String type = token.getHttpServletRequest().getHeader(AuthenticationProperties.SECURITY_FORM_TYPE_HEADER_NAME);
        if (getMobileType().contains(type)) {
            Map<String, Object> params = new LinkedHashMap<>();

            String tokenValue = token.getHttpServletRequest().getParameter(
                    memberUserRegisterConfig.getCaptchaTokenParamName()
            );

            params.put(memberUserRegisterConfig.getCaptchaTokenParamName(), tokenValue);
            params.put(memberUserRegisterConfig.getCaptchaParamName(), presentedPassword);
            params.put(memberUserRegisterConfig.getCaptchaUsernameParamName(), token.getPrincipal().toString());

            try {

                RestResult<Map<String, Object>> result = configServiceFeignClient.verifyCaptcha(params);

                if (result.getStatus() == HttpStatus.OK.value()) {
                    return true;
                } else {
                    throw new BadCredentialsException(result.getMessage());
                }

            } catch (Exception e) {
                throw new AuthenticationServiceException("调用验证码服务出现异常", e);
            }
        }
        return super.matchesPassword(presentedPassword, token, userDetails);
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
    public PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails, PrincipalAuthenticationToken token, Collection<? extends GrantedAuthority> grantedAuthorities) {
        if (Objects.nonNull(userDetails.getId())) {
            MemberUserEntity newOne = MemberUserEntity.of(userDetails);
            memberUserService.insert(newOne);
            userDetails.setId(newOne.getId());
            userDetails.getMeta().put(SecurityUserDetailsConstants.SECURITY_DETAILS_NEW_USER_KEY, true);
        }

        return super.createSuccessAuthentication(userDetails, token, grantedAuthorities);
    }
}
