package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
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
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 移动端认证授权服务
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
    public boolean preAuthenticationCache(PrincipalAuthenticationToken token, SecurityUserDetails userDetails, CacheProperties authenticationCache) {

        if (!RequestAuthenticationToken.class.isAssignableFrom(token.getClass())) {
            return true;
        }

        RequestAuthenticationToken requestAuthenticationToken = Casts.cast(token);
        if (!MobileUserDetails.class.isAssignableFrom(userDetails.getClass())) {
            return true;
        }

        String deviceId = requestAuthenticationToken
                .getHttpServletRequest()
                .getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        MobileUserDetails mobileUserDetails = Casts.cast(userDetails);
        if (!StringUtils.equals(mobileUserDetails.getDeviceIdentified(), deviceId)) {

            deviceIdContextRepository.deleteByMobileUserDetails(mobileUserDetails);

            RBucket<MobileUserDetails> mobileUserDetailsBucket = authorizationService
                    .getRedissonClient()
                    .getBucket(applicationConfig.getWakeUpCache().getName(mobileUserDetails.getDeviceIdentified()));
            mobileUserDetailsBucket.deleteAsync();

            mobileUserDetails.setDeviceIdentified(deviceId);
        }

        return super.preAuthenticationCache(token, userDetails, authenticationCache);
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException {
        String type = token.getHttpServletRequest().getHeader(authenticationProperties.getTypeHeaderName());

        String deviceId = token.getHttpServletRequest().getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        SystemUserEntity user;
        String password = StringUtils.EMPTY;

        if (StringUtils.isBlank(deviceId)) {
            throw new BadCredentialsException("移动端登陆没有找到唯一识别");
        }

        if (getMobileType().contains(type)) {

            user = getMobileTypeSystemUser(token);
            if (Objects.isNull(user)) {
                throw new BadCredentialsException("登陆账号于密码不正确");
            }
            password = user.getPassword();

        } else {
            user = getWakeUpTypeSystemUser(token);
            if (Objects.isNull(user)) {
                throw new BadCredentialsException("登陆账号于密码不正确");
            }
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(token.getHttpServletRequest());
        MobileUserDetails details = new MobileUserDetails(
                user.getId(),
                user.getUsername(),
                password,
                deviceId,
                device
        );

        ResourceSourceEnum source = Objects.requireNonNull(ResourceSourceEnum.of(token.getType()), "找不单类型为 [" + token.getType() + "] 的资源枚举");
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
     * 获取基础的认证用户明细
     *
     * @param token 请求认证 token
     * @return 系统用户
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
    public CacheProperties getAuthorizationCache(PrincipalAuthenticationToken token) {
        ResourceSourceEnum sourceEnum = ResourceSourceEnum.of(token.getType());
        Assert.isTrue(Objects.nonNull(sourceEnum), "找不到枚举值为 [" + token.getType() + "] 的资源来源类型");
        return CacheProperties.of(
                "dactiv:saas:" + DEFAULT_AUTHORIZATION_KEY_NAME + token + CacheProperties.DEFAULT_SEPARATOR + token.getPrincipal(),
                TimeProperties.of(7, TimeUnit.DAYS)
        );
    }

    @Override
    public CacheProperties getAuthenticationCache(PrincipalAuthenticationToken token) {
        ResourceSourceEnum sourceEnum = ResourceSourceEnum.of(token.getType());
        Assert.isTrue(Objects.nonNull(sourceEnum), "找不到枚举值为 [" + token.getType() + "] 的资源来源类型");
        return CacheProperties.of(
                "dactiv:saas:" + DEFAULT_AUTHENTICATION_KEY_NAME + sourceEnum + CacheProperties.DEFAULT_SEPARATOR + token.getPrincipal(),
                new TimeProperties(7, TimeUnit.DAYS)
        );
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
