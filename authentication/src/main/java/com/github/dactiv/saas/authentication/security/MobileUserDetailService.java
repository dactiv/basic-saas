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
import com.github.dactiv.saas.authentication.config.AccessTokenConfig;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.security.handler.CaptchaAuthenticationSuccessResponse;
import com.github.dactiv.saas.authentication.security.token.SchoolSourceAuthenticationToken;
import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.config.SchoolProperties;
import com.github.dactiv.saas.commons.domain.meta.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.PhoneInfoMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.WechatAccountMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.service.WechatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RBucket;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class MobileUserDetailService extends AbstractUserDetailsService<SystemUserEntity> {

    public static final String DEFAULT_CODE_PARAM_NAME = "schoolId";

    private final SchoolProperties schoolProperties;

    private final ApplicationConfig applicationConfig;

    private final PasswordEncoder passwordEncoder;

    private final AuthorizationService authorizationService;

    private final AuthenticationProperties authenticationProperties;

    private final AccessTokenConfig accessTokenConfig;

    private final WechatService wechatService;

    private final DeviceIdContextRepository deviceIdContextRepository;

    public MobileUserDetailService(SchoolProperties schoolProperties,
                                   ApplicationConfig applicationConfig,
                                   PasswordEncoder passwordEncoder,
                                   AuthorizationService authorizationService,
                                   AuthenticationProperties authenticationProperties,
                                   DeviceIdContextRepository deviceIdContextRepository,
                                   WechatService wechatService,
                                   AccessTokenConfig accessTokenConfig) {
        super(authenticationProperties);
        this.schoolProperties = schoolProperties;
        this.applicationConfig = applicationConfig;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
        this.authenticationProperties = authenticationProperties;
        this.deviceIdContextRepository = deviceIdContextRepository;
        this.wechatService = wechatService;
        this.accessTokenConfig = accessTokenConfig;
    }

    public abstract List<String> getMobileType();

    public abstract List<String> getWechatType();

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String type) {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        username = StringUtils.defaultString(username, StringUtils.EMPTY).trim();
        password = StringUtils.defaultString(password, StringUtils.EMPTY);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        if (getMobileType().contains(type)) {
            String code = request.getParameter(DEFAULT_CODE_PARAM_NAME);
            return new SchoolSourceAuthenticationToken(request, response, token, type, code);
        } else if (getWechatType().contains(type)) {
            SimpleWechatUserDetailsMeta meta = wechatAppletLogin(wechatService.getWechatProperties().getApplet(), username);
            WechatAuthenticationToken result = new WechatAuthenticationToken(request, response, type, meta);
            String phoneNumberCode = request.getParameter(wechatService.getWechatProperties().getPhoneNumberCodeParamName());

            if (StringUtils.isNotBlank(phoneNumberCode)) {
                PhoneInfoMeta phoneInfo = wechatService.getAppletPhoneNumber(phoneNumberCode);
                result.setPhoneInfo(phoneInfo);
            }

            return result;
        } else {
            return super.createToken(request, response, type);
        }
    }

    @Override
    public PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails, PrincipalAuthenticationToken token, Collection<? extends GrantedAuthority> grantedAuthorities) {

        if (WechatAuthenticationToken.class.isAssignableFrom(token.getClass())) {
            WechatAuthenticationToken wechatAuthenticationToken = Casts.cast(token);
            updateWechatSessionKey(userDetails, wechatAuthenticationToken);

            userDetails.getMeta().put(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_KEY, wechatAuthenticationToken.getUserDetails());
            userDetails.getMeta().put(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_PHONE_KEY, wechatAuthenticationToken.getPhoneInfo());
        }

        ResourceSourceEnum sourceEnum = ResourceSourceEnum.of(token.getType());
        Assert.isTrue(Objects.nonNull(sourceEnum), "找不到枚举值为 [" + token.getType() + "] 的资源来源类型");

        return new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials()),
                sourceEnum.toString(),
                userDetails,
                grantedAuthorities,
                false
        );
    }

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
    public void onSuccessAuthentication(PrincipalAuthenticationToken result, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String code = request.getParameter(wechatService.getWechatProperties().getSuccessAuthenticationBuildParamName());

        if (StringUtils.isNotBlank(code)) {
            SimpleWechatUserDetailsMeta meta = wechatAppletLogin(wechatService.getWechatProperties().getApplet(), code);
            buildWechatUserDetailsMeta(meta, result);
        }

    }

    protected abstract void buildWechatUserDetailsMeta(SimpleWechatUserDetailsMeta meta, PrincipalAuthenticationToken result);

    protected abstract void updateWechatSessionKey(SecurityUserDetails userDetails, WechatAuthenticationToken token);

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException {
        String type = token.getHttpServletRequest().getHeader(authenticationProperties.getTypeHeaderName());

        String deviceId = token.getHttpServletRequest().getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        SystemUserEntity user;
        String password = StringUtils.EMPTY;

        if (getMobileType().contains(type)) {

            if (StringUtils.isBlank(deviceId)) {
                throw new BadCredentialsException("移动端登陆没有找到唯一识别");
            }

            SchoolSourceAuthenticationToken schoolSourceAuthenticationToken = Casts.cast(token);
            if (!schoolProperties.getId().toString().equals(schoolSourceAuthenticationToken.getCode())) {
                throw new UsernameNotFoundException("用户名或密码错误");
            }

            user = getSchoolSourceTypeSystemUser(token);

            if (Objects.isNull(user)) {
                throw new UsernameNotFoundException("用户名或密码错误");
            }

            if (UserDetailsAccessToken.class.isAssignableFrom(user.getClass())) {
                UserDetailsAccessToken tokenUserDetails = Casts.cast(user);
                password = tokenUserDetails.getAccessToken();
            } else {
                password = user.getPassword();
            }

        } else if (getWechatType().contains(type)) {

            if (StringUtils.isBlank(deviceId)) {
                throw new BadCredentialsException("移动端登陆没有找到唯一识别");
            }

            WechatAuthenticationToken wechatAuthenticationToken = Casts.cast(token);

            user = getWechatTypeSystemUser(wechatAuthenticationToken);

            if (Objects.isNull(user)) {
                throw new AuthenticationCredentialsNotFoundException("通过微信信息找不到用户数据，请重新使用用户名密码登陆。");
            }
        } else {
            user = getBasicAuthenticationSystemUser(token);
            if (Objects.isNull(user)) {
                throw new UsernameNotFoundException("用户名或密码错误");
            }
            password = user.getPassword();
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        SecurityUserDetails details;
        if (getMobileType().contains(type) || getWechatType().contains(type)) {
            UserAgent device = DeviceUtils.getRequiredCurrentDevice(token.getHttpServletRequest());
            details = new MobileUserDetails(
                    user.getId(),
                    user.getUsername(),
                    password,
                    deviceId,
                    device
            );
        } else {
            details = new SecurityUserDetails(
                    user.getId(),
                    user.getUsername(),
                    password,
                    user.getStatus()
            );
        }

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

    @SuppressWarnings("unchecked")
    public SimpleWechatUserDetailsMeta wechatAppletLogin(WechatAccountMeta wechatAccountMeta, String code) {
        String url = MessageFormat.format("https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code", wechatAccountMeta.getAppId(), wechatAccountMeta.getSecret(), code);
        ResponseEntity<String> result = wechatService.getRestTemplate().getForEntity(url, String.class);

        String bodyString = StringUtils.defaultString(result.getBody(), StringUtils.EMPTY);
        if (StringUtils.isEmpty(bodyString)) {
            wechatService.throwSystemExceptionIfError(new LinkedHashMap<>());
        }

        Map<String, Object> body = Casts.readValue(result.getBody(), Map.class);
        if (log.isDebugEnabled()) {
            log.debug("微信小程序登陆，响应结果为:" + body);
        }
        if (wechatService.isSuccess(new ResponseEntity<>(body, result.getHeaders(), result.getStatusCode()))) {
            return SimpleWechatUserDetailsMeta.of(body);
        } else {
            wechatService.throwSystemExceptionIfError(body);
        }

        return null;
    }

    /**
     * 获取基础的认证用户明细
     *
     * @param token 请求认证 token
     * @return 系统用户
     */
    protected abstract SystemUserEntity getBasicAuthenticationSystemUser(RequestAuthenticationToken token);

    /**
     * 获取符合学校来源类型的系统用户
     *
     * @param token 请求认证 token
     * @return 系统用户
     */
    protected abstract SystemUserEntity getSchoolSourceTypeSystemUser(RequestAuthenticationToken token);

    /**
     * 获取微信来源类型的系统用户
     *
     * @param token 请求认证 token
     * @return 系统用户
     */
    protected abstract SystemUserEntity getWechatTypeSystemUser(RequestAuthenticationToken token);

    @Override
    public boolean matchesPassword(String presentedPassword,
                                   RequestAuthenticationToken token,
                                   SecurityUserDetails userDetails) {

        String type = token.getHttpServletRequest().getHeader(AuthenticationProperties.SECURITY_FORM_TYPE_HEADER_NAME);
        if (getMobileType().contains(type)) {
            ByteSource byteSource = deviceIdContextRepository
                    .getCipherAlgorithmService()
                    .getCipherService(CipherAlgorithmService.AES_ALGORITHM)
                    .decrypt(Base64.decode(presentedPassword), Base64.decode(schoolProperties.getAccessKey()));

            String decryptPresentedPassword = new String(byteSource.obtainBytes(), StandardCharsets.UTF_8);
            String timeValue = StringUtils.substringAfter(decryptPresentedPassword, accessTokenConfig.getSeparator());

            if (StringUtils.isBlank(timeValue)) {
                return false;
            }

            long time = NumberUtils.toLong(timeValue);
            long useTime = accessTokenConfig.getExpirationTime().getUnit().toChronoUnit().between(Instant.ofEpochMilli(time), Instant.now());
            if (useTime > accessTokenConfig.getExpirationTime().getValue()) {
                return false;
            }

            String password = StringUtils.substringBefore(decryptPresentedPassword, accessTokenConfig.getSeparator());
            return StringUtils.equals(password, userDetails.getPassword());
        } else if (getWechatType().contains(type)) {
            return true;
        } else if (ResourceSourceEnum.WAKE_UP_SOURCE_VALUE.equals(type)) {
            ByteSource byteSource = deviceIdContextRepository
                    .getCipherAlgorithmService()
                    .getCipherService(CipherAlgorithmService.AES_ALGORITHM)
                    .decrypt(presentedPassword.getBytes(StandardCharsets.UTF_8), schoolProperties.getAccessKey().getBytes(StandardCharsets.UTF_8));

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
}
