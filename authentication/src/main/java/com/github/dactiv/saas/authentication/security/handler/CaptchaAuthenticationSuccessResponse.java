package com.github.dactiv.saas.authentication.security.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.consumer.ValidAuthenticationInfoConsumer;
import com.github.dactiv.saas.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.saas.authentication.domain.meta.IpRegionMeta;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class CaptchaAuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler;

    private final PasswordEncoder passwordEncoder;

    private final AmqpTemplate amqpTemplate;

    private final DeviceIdContextRepository deviceIdContextRepository;

    private final ApplicationConfig applicationConfig;

    private final RedissonClient redissonClient;

    public CaptchaAuthenticationSuccessResponse(CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler,
                                                PasswordEncoder passwordEncoder,
                                                ApplicationConfig applicationConfig,
                                                DeviceIdContextRepository deviceIdContextRepository,
                                                RedissonClient redissonClient,
                                                AmqpTemplate amqpTemplate) {
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.passwordEncoder = passwordEncoder;
        this.applicationConfig = applicationConfig;
        this.deviceIdContextRepository = deviceIdContextRepository;
        this.redissonClient = redissonClient;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();
        SecurityUserDetails userDetails = Casts.cast(details);

        jsonAuthenticationFailureHandler.deleteAllowableFailureNumber(request);

        AuthenticationInfoEntity info = new AuthenticationInfoEntity();

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(request);

        info.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(userDetails));
        info.setDevice(device.toMap());
        info.setMeta(new LinkedHashMap<>(userDetails.getMeta()));

        String ip = SpringMvcUtils.getIpAddress(request);

        //noinspection unchecked
        info.setIpRegion(Casts.convertValue(IpRegionMeta.of(ip), Map.class));
        info.setRetryCount(0);

        if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            MobileUserDetails mobileUserDetails = Casts.cast(details);
            Map<String, Object> data = createMobileAuthenticationResult(mobileUserDetails);
            result.setData(data);
        }

        amqpTemplate.convertAndSend(
                SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE,
                ValidAuthenticationInfoConsumer.DEFAULT_QUEUE_NAME,
                info
        );

        if (MapUtils.isNotEmpty(userDetails.getMeta())) {
            Map<String, Object> meta = new LinkedHashMap<>(userDetails.getMeta());
            meta.remove(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_PHONE_KEY);
            meta.remove(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_KEY);
            userDetails.setMeta(meta);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createMobileAuthenticationResult(MobileUserDetails details) {

        int randomPasswordCount = applicationConfig.getRandomPasswordCount();
        details.setPassword(RandomStringUtils.randomAlphanumeric(randomPasswordCount));

        Map<String, Object> result = Casts.convertValue(details, Map.class);
        //String text = details.getUsername() + CacheProperties.DEFAULT_SEPARATOR + details.getDeviceIdentified() + CacheProperties.DEFAULT_SEPARATOR + System.currentTimeMillis();
        ByteSource byteSource = deviceIdContextRepository.createToken(details);

        result.put(applicationConfig.getWakeUpParamName(), byteSource.getBase64());
        String password = createPassword(byteSource.getBase64(), details.getUsername(), details.getDeviceIdentified());

        password = appendPasswordString(password, details.getDevice());

        if (log.isDebugEnabled()) {
            log.debug("创建 MobileUserDetails token，当前 MobileUserDetails 密码为:"
                    + password + ",原文为:" + byteSource.getBase64() + details.getUsername() + details.getDeviceIdentified());
        }

        details.setPassword(passwordEncoder.encode(password));

        saveMobileUserDetails(details);

        return result;
    }

    /**
     * 保存移动设备用户明细到缓存
     *
     * @param details 移动设备用户明细
     */
    private void saveMobileUserDetails(MobileUserDetails details) {
        if (Objects.isNull(applicationConfig.getWakeUpCache())) {
            return;
        }

        RBucket<MobileUserDetails> bucket = redissonClient.getBucket(applicationConfig.getWakeUpCache().getName(details.getDeviceIdentified()));

        TimeProperties time = applicationConfig.getWakeUpCache().getExpiresTime();

        if (Objects.nonNull(time)) {
            bucket.setAsync(details, time.getValue(), time.getUnit());
        } else {
            bucket.setAsync(details);
        }
    }

    /**
     * 追加密码字符串
     *
     * @param password 密码
     * @param device   设备信息
     * @return 新的字符串内容
     */
    public static String appendPasswordString(String password, UserAgent device) {
        return password + device.toString();
    }

    public static String createPassword(String token, String username, String deviceIdentified) {
        return DigestUtils.md5DigestAsHex(
                (token + username + deviceIdentified).getBytes()
        );
    }
}
