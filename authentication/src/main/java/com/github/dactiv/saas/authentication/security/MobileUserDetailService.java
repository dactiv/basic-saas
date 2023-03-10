package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.security.handler.CaptchaAuthenticationSuccessResponse;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * ???????????????????????????
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class MobileUserDetailService extends AbstractUserDetailsService<SystemUserEntity> {

    public static final String IS_MOBILE_PATTERN_STRING = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";

    protected final ApplicationConfig applicationConfig;

    protected final PasswordEncoder passwordEncoder;

    private final AuthorizationService authorizationService;

    private final AuthenticationProperties authenticationProperties;

    private final DeviceIdContextRepository deviceIdContextRepository;

    public MobileUserDetailService(ApplicationConfig applicationConfig,
                                   PasswordEncoder passwordEncoder,
                                   AuthorizationService authorizationService,
                                   AuthenticationProperties authenticationProperties,
                                   DeviceIdContextRepository deviceIdContextRepository) {
        super(authenticationProperties);
        this.applicationConfig = applicationConfig;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
        this.authenticationProperties = authenticationProperties;
        this.deviceIdContextRepository = deviceIdContextRepository;
    }

    public abstract List<String> getMobileType();

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException {
        String type = token.getHttpServletRequest().getHeader(authenticationProperties.getTypeHeaderName());

        String deviceId = token.getHttpServletRequest().getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        SystemUserEntity user;
        String password = StringUtils.EMPTY;

        if (StringUtils.isBlank(deviceId)) {
            throw new BadCredentialsException("???????????????????????????????????????");
        }

        if (getMobileType().contains(type)) {

            user = getMobileTypeSystemUser(token);
            if (Objects.isNull(user)) {
                throw new BadCredentialsException("??????????????????????????????");
            }
            password = user.getPassword();

        } else {
            user = getWakeUpTypeSystemUser(token);
            if (Objects.isNull(user)) {
                throw new BadCredentialsException("??????????????????????????????");
            }
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("???????????????????????????");
        }

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(token.getHttpServletRequest());
        MobileUserDetails details = new MobileUserDetails(
                user.getId(),
                user.getUsername(),
                password,
                deviceId,
                device
        );

        ResourceSourceEnum source = Objects.requireNonNull(ResourceSourceEnum.of(token.getType()), "?????????????????? [" + token.getType() + "] ???????????????");
        RoleAuthority role = new RoleAuthority(source.getName(), source.toString());
        List<RoleAuthority> roles = new LinkedList<>();
        roles.add(role);

        details.setRoleAuthorities(roles);

        authorizationService.setSystemUserAuthorities(user, details);

        details.setMeta(user.toSecurityUserDetailsMeta());
        details.setType(source.toString());

        return details;
    }

    protected abstract SystemUserEntity getWakeUpTypeSystemUser(RequestAuthenticationToken token);

    /**
     * ?????????????????????????????????
     *
     * @param token ???????????? token
     * @return ????????????
     */
    protected abstract SystemUserEntity getMobileTypeSystemUser(RequestAuthenticationToken token);

    @Override
    public boolean matchesPassword(String presentedPassword,
                                   RequestAuthenticationToken token,
                                   SecurityUserDetails userDetails) {

        String type = token.getHttpServletRequest().getHeader(AuthenticationProperties.SECURITY_FORM_TYPE_HEADER_NAME);
        if (ResourceSourceEnum.APP_WAKE_UP_SOURCE_VALUE.equals(type)) {
            ByteSource byteSource = deviceIdContextRepository
                    .getCipherAlgorithmService()
                    .getCipherService(CipherAlgorithmService.AES_ALGORITHM)
                    .decrypt(presentedPassword.getBytes(StandardCharsets.UTF_8), applicationConfig.getMobileAuthenticationSecretKey().getBytes(StandardCharsets.UTF_8));

            String text = Base64.decodeToString(byteSource.getBase64());
            String deviceIdentified = StringUtils.substringBetween(text, CacheProperties.DEFAULT_SEPARATOR, CacheProperties.DEFAULT_SEPARATOR);

            UserAgent device = DeviceUtils.getRequiredCurrentDevice(token.getHttpServletRequest());
            String password = CaptchaAuthenticationSuccessResponse.createPassword(presentedPassword, token.getPrincipal().toString(), deviceIdentified);
            String matchesPassword = CaptchaAuthenticationSuccessResponse.appendPasswordString(password, device);

            RBucket<MobileUserDetails> bucket = authorizationService.getRedissonClient().getBucket(applicationConfig.getWakeUpCache().getName(deviceIdentified));

            return bucket.isExists() && getPasswordEncoder().matches(matchesPassword, bucket.get().getPassword());
        } else {
            return super.matchesPassword(presentedPassword, token, userDetails);
        }
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public List<String> getType() {
        List<String> result = new LinkedList<>();
        result.add(ResourceSourceEnum.APP_WAKE_UP_SOURCE_VALUE);

        if (CollectionUtils.isNotEmpty(getMobileType())) {
            CollectionUtils.addAll(result, getMobileType());
        }

        return result;
    }
}
