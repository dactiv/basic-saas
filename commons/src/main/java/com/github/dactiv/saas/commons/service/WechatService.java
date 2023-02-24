package com.github.dactiv.saas.commons.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.saas.commons.config.WechatProperties;
import com.github.dactiv.saas.commons.domain.dto.wechat.AppletSubscribeMessageDto;
import com.github.dactiv.saas.commons.domain.dto.wechat.OfficialTemplateMessageDto;
import com.github.dactiv.saas.commons.domain.meta.wechat.AccessTokenMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.PhoneInfoMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.WechatAccountMeta;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 微信服务
 *
 * @author maurice.chen
 */
@Slf4j
public class WechatService implements InitializingBean {

    @Getter
    private final WechatProperties wechatProperties;

    @Getter
    private final RestTemplate restTemplate;

    private final RedissonClient redissonClient;

    public WechatService(WechatProperties wechatProperties, RestTemplate restTemplate, RedissonClient redissonClient) {
        this.wechatProperties = wechatProperties;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 获取微信访问 token
     *
     * @return 访问 token
     */
    @Concurrent(value = "dactiv:saas:wechat:get-wechat-access-token", waitTime = @Time(value = 8, unit = TimeUnit.SECONDS), leaseTime = @Time(value = 5, unit = TimeUnit.SECONDS), exception = "获取微信访问密钥出现并发")
    public AccessTokenMeta getWechatAccessToken(WechatAccountMeta wechatAccountMeta) {
        RBucket<AccessTokenMeta> bucket = redissonClient.getBucket(wechatAccountMeta.getAccessTokenCache().getName(wechatAccountMeta.getAppId()));
        AccessTokenMeta token = bucket.get();
        if (Objects.nonNull(token) && System.currentTimeMillis() - token.getCreationTime().getTime() > wechatAccountMeta.getGetAccessTokenLeadTime().toMillis()) {
            return token;
        }

        String url = MessageFormat.format("https://api.weixin.qq.com/cgi-bin/token?appid={0}&secret={1}&grant_type=client_credential", wechatAccountMeta.getAppId(), wechatAccountMeta.getSecret());
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(new LinkedHashMap<String, Object>()),
                new ParameterizedTypeReference<>() {
                }
        );
        log.info("获取 wechat access token 结果为:" + result.getBody());
        if (isSuccess(result)) {
            token = new AccessTokenMeta();
            //noinspection ConstantConditions
            token.setToken(result.getBody().get("access_token").toString());
            token.setExpiresTime(TimeProperties.of(NumberUtils.toInt(result.getBody().get("expires_in").toString()), TimeUnit.SECONDS));
            bucket.setAsync(token, token.getExpiresTime().getValue(), token.getExpiresTime().getUnit());
        } else {
            throwSystemExceptionIfError(result.getBody());
        }

        return token;
    }

    /**
     * 发送公众号模版消息
     *
     * @param wechatAccount 微信账户
     * @param openId        收消息的 open id
     * @param templateId    模版消息 id
     * @param url           点击跳转的 url
     * @param data          消息信息
     */
    public void sendOfficialTemplateMessage(WechatAccountMeta wechatAccount,
                                            String openId,
                                            String templateId,
                                            String url,
                                            TreeMap<String, TreeMap<String, String>> data) {
        AccessTokenMeta token = getWechatAccessToken(wechatAccount);
        OfficialTemplateMessageDto message = OfficialTemplateMessageDto.of(openId, templateId);

        if (StringUtils.isNotEmpty(url)) {
            message.setUrl(url);
        }

        message.setData(data);

        sendOfficialTemplateMessage(token.getToken(), message);
    }

    /**
     * 发送公众号模版消息
     *
     * @param wechatAccount 微信账户
     * @param message       模版消息实体
     */
    public void sendOfficialTemplateMessage(WechatAccountMeta wechatAccount, OfficialTemplateMessageDto message) {
        AccessTokenMeta token = getWechatAccessToken(wechatAccount);
        sendOfficialTemplateMessage(token.getToken(), message);
    }

    /**
     * 发送公众号模版消息
     *
     * @param token   访问 token 值
     * @param message 模版消息实体
     */
    public void sendOfficialTemplateMessage(String token, OfficialTemplateMessageDto message) {
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(
                "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token,
                HttpMethod.POST,
                new HttpEntity<>(message),
                new ParameterizedTypeReference<>() {
                }
        );

        if (!isSuccess(result)) {
            throwSystemExceptionIfError(result.getBody());
        }

        log.info("发送模版 ID 为 [" + message.getTemplateId() + "] 的数据到[" + message.getOpenId() + "] 用户相应结果为:" + result.getBody());
    }

    /**
     * 发送公众号模版消息
     *
     * @param wechatAccount 微信账户
     * @param message       订阅消息实体
     */
    public void sendAppletSubscribeMessage(WechatAccountMeta wechatAccount, AppletSubscribeMessageDto message) {
        AccessTokenMeta token = getWechatAccessToken(wechatAccount);
        sendAppletSubscribeMessage(token.getToken(), message);
    }

    /**
     * 发送公众号模版消息
     *
     * @param token   访问 token 值
     * @param message 订阅消息实体
     */
    public void sendAppletSubscribeMessage(String token, AppletSubscribeMessageDto message) {
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(
                "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token,
                HttpMethod.POST,
                new HttpEntity<>(message),
                new ParameterizedTypeReference<>() {
                }
        );

        if (!isSuccess(result)) {
            throwSystemExceptionIfError(result.getBody());
        }

        log.info("发送模版 ID 为 [" + message.getTemplateId() + "] 的数据到[" + message.getOpenId() + "] 用户相应结果为:" + result.getBody());
    }

    @Override
    @NacosCronScheduled(cron = "dactiv:saas:wechat:scheduled-update-wechat-access-token")
    @Concurrent(value = "dactiv:saas:wechat:update-wechat-access-token", waitTime = @Time(0), exception = "更新微信访问密钥出现并发")
    public void afterPropertiesSet() {
        try {
            getWechatAccessToken(wechatProperties.getApplet());
            getWechatAccessToken(wechatProperties.getOfficial());
        } catch (Exception e) {
            log.error("获取微信访问 token 出错", e);
        }
    }

    /**
     * 获取小程序手机好吗
     *
     * @param code 手机号获取凭证
     * @return 微信手机号码元数据信息
     */
    public PhoneInfoMeta getAppletPhoneNumber(String code) {
        AccessTokenMeta token = getWechatAccessToken(wechatProperties.getApplet());
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(
                "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + token.getToken(),
                HttpMethod.POST,
                new HttpEntity<>(body, new LinkedMultiValueMap<>()),
                new ParameterizedTypeReference<>() {
                }
        );

        if (isSuccess(result)) {
            //noinspection ConstantConditions
            return new PhoneInfoMeta(Casts.cast(result.getBody().get("phone_info")));
        } else {
            throwSystemExceptionIfError(result.getBody());
        }

        return null;
    }

    /**
     * 是否调用成功
     *
     * @param result 响应实体
     * @return true 是，否则 false
     */
    public boolean isSuccess(ResponseEntity<Map<String, Object>> result) {

        if (!HttpStatus.OK.equals(result.getStatusCode())) {
            return false;
        }

        if (MapUtils.isEmpty(result.getBody())) {
            return false;
        }

        if (result.getBody().containsKey(wechatProperties.getStatusCodeFieldName())) {
            return false;
        }

        return !result.getBody().containsKey(wechatProperties.getStatusCodeFieldName()) || !result.getBody().get(wechatProperties.getStatusCodeFieldName()).equals(wechatProperties.getSuccessCodeValue());
    }

    /**
     * 如果响应内容错误，抛出异常
     *
     * @param result 响应数据
     */
    public void throwSystemExceptionIfError(Map<String, Object> result) {
        if (MapUtils.isNotEmpty(result)) {
            throw new SystemException("[" + result.get(wechatProperties.getStatusCodeFieldName()) + "]:" + result.get(wechatProperties.getStatusMessageFieldName()));
        } else {
            throw new SystemException("执行微信 api 接口出现异常");
        }
    }
}
