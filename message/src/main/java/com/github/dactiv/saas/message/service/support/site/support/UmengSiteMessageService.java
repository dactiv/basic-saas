package com.github.dactiv.saas.message.service.support.site.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.feign.AuthenticationServiceFeignClient;
import com.github.dactiv.saas.message.config.site.SiteConfig;
import com.github.dactiv.saas.message.config.site.umeng.Android;
import com.github.dactiv.saas.message.config.site.umeng.Ios;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.saas.message.domain.meta.site.umeng.BasicMessageMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.PolicyMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.android.AndroidMessageMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.android.AndroidPayloadBodyMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.android.AndroidPayloadMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.android.AndroidPolicyMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.ios.IosPayloadApsAlertMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.ios.IosPayloadApsMeta;
import com.github.dactiv.saas.message.domain.meta.site.umeng.ios.IosPayloadMeta;
import com.github.dactiv.saas.message.enumerate.site.ument.UmengMessageTypeEnum;
import com.github.dactiv.saas.message.service.support.site.SiteMessageChannelSender;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ???????????????????????????
 *
 * @author maurice
 */
@Component
public class UmengSiteMessageService implements SiteMessageChannelSender {

    /**
     * ????????????
     */
    public static final String DEFAULT_TYPE = "umeng";

    /**
     * ?????????????????????????????????
     */
    public static final String DEFAULT_USER_ALIAS_TYPE = "USER_ID";

    /**
     * ??????????????????????????????
     */
    public static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";

    public static final String DEFAULT_RESULT_FIELD = "ret";

    private final SiteConfig config;

    private final RestTemplate restTemplate;

    private final AuthenticationServiceFeignClient authenticationServiceFeignClient;

    public UmengSiteMessageService(SiteConfig config, RestTemplate restTemplate, AuthenticationServiceFeignClient authenticationServiceFeignClient) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.authenticationServiceFeignClient = authenticationServiceFeignClient;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RestResult<Map<String, Object>> sendSiteMessage(SiteMessageEntity message) {

        BasicUserDetails<Integer> userDetails = BasicUserDetails.of(
                message.getUserId(),
                message.getUsername(),
                message.getUserType()
        );
        // ??????????????????????????????
        Map<String, Object> info = authenticationServiceFeignClient.getLastAuthenticationInfo(userDetails);

        if (MapUtils.isEmpty(info)) {
            return RestResult.of(
                    "id ???[" + message.getUserId() + "]??????????????????????????????????????????????????????",
                    HttpStatus.NOT_FOUND.value(),
                    String.valueOf(HttpStatus.NOT_FOUND.value())
            );
        }

        // ??????????????????
        Map<String, Object> device = Casts.cast(info.get(AuthenticationServiceFeignClient.AUTHENTICATION_INFO_DEVICE_FIELD_NAME), Map.class);

        BasicMessageMeta basicMessage = null;
        // ??????????????????????????????????????????
        if (Android.NAME.equals(device.get(UserAgent.OPERATING_SYSTEM_NAME))) {
            basicMessage = getAndroidMessage(message, UmengMessageTypeEnum.Customize);
        } else if (Ios.NAME.equals(device.get(UserAgent.OPERATING_SYSTEM_NAME))) {
            basicMessage = getIosMessage(message, UmengMessageTypeEnum.Customize);
        }

        if (basicMessage == null) {
            return RestResult.of(
                    device + "?????????????????????????????????",
                    HttpStatus.NOT_FOUND.value(),
                    String.valueOf(HttpStatus.NOT_FOUND.value())
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = Casts.writeValueAsString(basicMessage);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        String sign = DigestUtils.md5Hex((WebContentGenerator.METHOD_POST + config.getUmeng().getUrl() + requestJson + basicMessage.getSecretKey()).getBytes(StandardCharsets.UTF_8));
        // ????????????
        ResponseEntity<String> result = restTemplate.postForEntity(config.getUmeng().getUrl() + "?sign=" + sign, entity, String.class);
        // OK ????????????????????????
        if (result.getStatusCode().equals(HttpStatus.OK)) {

            Map<String, Object> resultBody = Casts.readValue(result.getBody(), Map.class);
            Map<String, Object> data = new LinkedHashMap<>();
            if (resultBody.containsKey(RestResult.DEFAULT_DATA_NAME)) {
                data = Casts.cast(resultBody.get(RestResult.DEFAULT_DATA_NAME), Map.class);
            }

            if (DEFAULT_SUCCESS_MESSAGE.equals(resultBody.get(DEFAULT_RESULT_FIELD))) {
                return RestResult.of(
                        "id ???[" + message.getId() + "] ????????????????????? [" + message.getUserId() + "] ???????????????",
                        result.getStatusCode().value(),
                        String.valueOf(result.getStatusCode().value()),
                        data
                );
            } else {
                return RestResult.of(
                        resultBody.get(DEFAULT_RESULT_FIELD).toString(),
                        result.getStatusCode().value(),
                        ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                        data
                );
            }
        }

        return RestResult.of(
                HttpStatus.valueOf(result.getStatusCode().value()).getReasonPhrase(),
                result.getStatusCode().value(),
                ErrorCodeException.DEFAULT_EXCEPTION_CODE
        );
    }

    /**
     * ?????? ios ????????????
     *
     * @param entity ???????????????
     * @return ??????????????????
     */
    @SuppressWarnings("unchecked")
    public BasicMessageMeta getIosMessage(SiteMessageEntity entity, UmengMessageTypeEnum type) {
        BasicMessageMeta result = new BasicMessageMeta();

        result.setProductionMode(config.getUmeng().isProductionMode());
        result.setAppkey(config.getUmeng().getIos().getAppKey());
        result.setSecretKey(config.getUmeng().getIos().getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getUserId().toString());

        IosPayloadMeta iosPayload = new IosPayloadMeta();
        IosPayloadApsMeta iosPayloadAps = new IosPayloadApsMeta();
        iosPayload.setAps(iosPayloadAps);

        IosPayloadApsAlertMeta iosPayloadApsAlert = new IosPayloadApsAlertMeta();
        iosPayloadApsAlert.setTitle(entity.getTitle());
        iosPayloadApsAlert.setBody(entity.getContent());

        iosPayloadAps.setAlert(iosPayloadApsAlert);

        Map<String, Object> payloadMap = Casts.convertValue(iosPayload, Map.class);

        payloadMap.putAll(entity.getMeta());
        //payloadMap.put("type", entity.getType());

        result.setPayload(payloadMap);

        PolicyMeta policy = new PolicyMeta();

        policy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(policy);

        result.setDescription(Ios.NAME);

        return result;
    }

    /**
     * ????????????????????????
     *
     * @param entity ???????????????
     * @return ??????????????????
     */
    public BasicMessageMeta getAndroidMessage(SiteMessageEntity entity, UmengMessageTypeEnum type) {

        AndroidMessageMeta result = new AndroidMessageMeta();

        result.setProductionMode(config.getUmeng().isProductionMode());
        result.setAppkey(config.getUmeng().getAndroid().getAppKey());
        result.setSecretKey(config.getUmeng().getAndroid().getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getUserId().toString());

        AndroidPayloadMeta androidPayload = new AndroidPayloadMeta();

        androidPayload.setDisplayType("notification");

        androidPayload.getExtra().putAll(entity.getMeta());

        AndroidPayloadBodyMeta androidPayloadBody = new AndroidPayloadBodyMeta();

        androidPayloadBody.setTicker(entity.getContent());
        androidPayloadBody.setTitle(entity.getTitle());
        androidPayloadBody.setText(entity.getContent());

        androidPayloadBody.setAfterOpen("go_app");

        androidPayload.setBody(androidPayloadBody);
        result.setPayload(androidPayload);

        AndroidPolicyMeta androidPolicy = new AndroidPolicyMeta();

        androidPolicy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(androidPolicy);

        result.setDescription(Android.NAME);

        if (!config.getUmeng().getAndroid().getIgnoreActivityType().contains(entity.getType())) {
            result.setMipush(config.getUmeng().getAndroid().isPush());
            result.setMiActivity(config.getUmeng().getAndroid().getActivity());
        }

        return result;
    }

    public Date getExpireTime(Date currentDate) {

        LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plus(config.getUmeng().getExpireTime().getValue(), config.getUmeng().getExpireTime().getUnit().toChronoUnit());

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
