package com.github.dactiv.saas.authentication.security.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.authentication.rememberme.RememberMeToken;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.enumerate.LoginTypeEnum;
import com.github.dactiv.saas.authentication.plugin.PluginResourceService;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * json ?????????????????????????????????
 *
 * @author maurice.chen
 */
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * ???????????????????????????
     */
    public final static String DEFAULT_IS_AUTHENTICATION_NAME = "authentication";

    /**
     * ???????????????????????????
     */
    private final static String DEFAULT_SERVICES_NAME = "services";

    /**
     * ???????????????????????????
     */
    private final static String DEFAULT_PLUGIN_NAME = "pluginServices";

    /**
     * ????????? token ??????
     */
    public final static String DEFAULT_TOKEN_NAME = "token";

    private final ApplicationConfig applicationConfig;

    private final CaptchaAuthenticationFailureResponse failureHandler;

    private final AuthorizationService authorizationService;

    private final CookieRememberService cookieRememberService;

    private final DeviceIdContextRepository deviceIdContextRepository;

    private final DiscoveryClient discoveryClient;

    private final PluginResourceService pluginResourceService;

    public JsonLogoutSuccessHandler(ApplicationConfig applicationConfig,
                                    CaptchaAuthenticationFailureResponse failureHandler,
                                    AuthorizationService authorizationService,
                                    CookieRememberService cookieRememberService,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    DiscoveryClient discoveryClient,
                                    PluginResourceService pluginResourceService) {
        this.applicationConfig = applicationConfig;
        this.failureHandler = failureHandler;
        this.authorizationService = authorizationService;
        this.cookieRememberService = cookieRememberService;
        this.deviceIdContextRepository = deviceIdContextRepository;
        this.discoveryClient = discoveryClient;
        this.pluginResourceService = pluginResourceService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpStatus httpStatus = SpringMvcUtils.getHttpStatus(response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication != null && SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {
            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails(), SecurityUserDetails.class);
            clearAllCache(userDetails);
            RBucket<RememberMeToken> rememberMeToken = cookieRememberService.getRememberMeTokenBucket(userDetails.toBasicUserDetails());
            rememberMeToken.deleteAsync();
        }

        cookieRememberService.loginFail(request, response);

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>());

        response.getWriter().write(Casts.writeValueAsString(result));
    }

    /**
     * ??????????????????
     *
     * @param userDetails ????????????
     */
    private void clearAllCache(SecurityUserDetails userDetails) {

        // ?????? username ????????????????????????????????????
        clearPrincipalCache(userDetails);

        if (MobileUserDetails.class.isAssignableFrom(userDetails.getClass())) {
            MobileUserDetails mobileUserDetails = Casts.cast(userDetails);
            deviceIdContextRepository.deleteByMobileUserDetails(mobileUserDetails);

            RBucket<MobileUserDetails> mobileUserDetailsBucket = authorizationService
                    .getRedissonClient()
                    .getBucket(applicationConfig.getWakeUpCache().getName(mobileUserDetails.getDeviceIdentified()));
            mobileUserDetailsBucket.deleteAsync();
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param userDetails ??????????????????
     */
    private void clearPrincipalCache(SecurityUserDetails userDetails) {

        SimpleAuthenticationToken token = new SimpleAuthenticationToken(
                userDetails.getUsername(),
                userDetails.getType(),
                false
        );
        authorizationService.deleteSecurityUserDetailsCache(token);
        authorizationService.deleteRememberMeCache(token);

    }

    /**
     * ??????????????? reset ????????????????????????????????????????????????????????????????????????
     *
     * @param request ????????????
     * @return rest ?????????
     */
    public RestResult<Map<String, Object>> createUnauthorizedResult(HttpServletRequest request) {

        RestResult<Map<String, Object>> result = createRestResult(request);
        postCaptchaData(result, request);

        result.getData().put(DEFAULT_SERVICES_NAME, discoveryClient.getServices());
        result.getData().put(DEFAULT_PLUGIN_NAME, pluginResourceService.getPluginServerNames());

        return result;
    }

    /**
     * ?????? reset ?????????
     *
     * @return reset ?????????
     */
    private RestResult<Map<String, Object>> createRestResult(HttpServletRequest request) {

        String executeCode = String.valueOf(HttpStatus.OK.value());
        String message = HttpStatus.OK.getReasonPhrase();
        int status = HttpStatus.OK.value();

        Map<String, Object> data = new LinkedHashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) ||
                AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass())) {
            data.put(DEFAULT_IS_AUTHENTICATION_NAME, false);
            message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        } else {
            data.put(DEFAULT_IS_AUTHENTICATION_NAME, authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
            }

            if (PrincipalAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
                PrincipalAuthenticationToken authenticationToken = Casts.cast(authentication);
                data.put(RememberMeProperties.DEFAULT_PARAM_NAME, authenticationToken.isRememberMe());

                if (authenticationToken.isRememberMe()) {
                    data.put(RememberMeProperties.DEFAULT_USER_DETAILS_NAME, authentication.getDetails());
                }
            }
        }

        String identified = StringUtils.defaultString(
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME),
                UUID.randomUUID().toString()
        );

        data.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, identified);

        return new RestResult<>(
                message,
                status,
                executeCode,
                data
        );
    }

    /**
     * ?????????????????????
     *
     * @param result  reset ?????????
     * @param request http ????????????
     */
    private void postCaptchaData(RestResult<Map<String, Object>> result, HttpServletRequest request) {
        Integer number = failureHandler.getAllowableFailureNumber(request);

        Integer allowableFailureNumber = applicationConfig.getAllowableFailureNumber();

        if (number < allowableFailureNumber) {
            return;
        }

        // ????????????????????????
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String type = request.getParameter(CaptchaAuthenticationFailureResponse.DEFAULT_TYPE_PARAM_NAME);
        // ???????????????????????????????????????????????????????????????????????????????????? token ??????????????????????????????
        // ????????????????????? CaptchaAuthenticationFilter ?????????????????????????????? CaptchaAuthenticationFilter???
        // ?????????????????????????????????????????????????????????????????????????????????????????? token ???????????????
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (LoginTypeEnum.Mobile.toString().equals(type)) {

            Map<String, Object> buildToken = failureHandler
                    .getConfigServiceFeignClient()
                    .generateToken(
                            CaptchaAuthenticationFailureResponse.DEFAULT_MOBILE_CAPTCHA_TYPE,
                            identified
                    );

            String token = buildToken.get(DEFAULT_TOKEN_NAME).toString();

            String captchaType = applicationConfig.getAppLoginFailureCaptchaType();

            Map<String, Object> interceptToken = failureHandler
                    .getConfigServiceFeignClient()
                    .createGenerateCaptchaIntercept(
                            buildToken.get(DEFAULT_TOKEN_NAME).toString(),
                            captchaType,
                            CaptchaAuthenticationFailureResponse.DEFAULT_MOBILE_CAPTCHA_TYPE
                    );

            result.getData().put(DEFAULT_TOKEN_NAME, token);
            result.getData().putAll(interceptToken);

        } else {
            String captchaType = applicationConfig.getFormLoginFailureCaptchaType();

            Map<String, Object> buildToken = failureHandler
                    .getConfigServiceFeignClient()
                    .generateToken(captchaType, identified);

            result.getData().putAll(buildToken);
        }
        result.setExecuteCode(CaptchaAuthenticationFailureResponse.CAPTCHA_EXECUTE_CODE);
    }
}
