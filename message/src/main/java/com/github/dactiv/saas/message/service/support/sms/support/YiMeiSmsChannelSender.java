package com.github.dactiv.saas.message.service.support.sms.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.saas.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.saas.message.config.sms.YiMeiSmsConfig;
import com.github.dactiv.saas.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.saas.message.domain.meta.SmsBalanceMeta;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 亿美短信渠道发送者实现
 *
 * @author maurice
 */
@Component
@SuppressWarnings("unchecked")
public class YiMeiSmsChannelSender implements SmsChannelSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(YiMeiSmsChannelSender.class);

    private static final String DEFAULT_SEND_SMS_API = "/simpleinter/sendSMS";

    private static final String DEFAULT_GET_BALANCE_API = "/simpleinter/getBalance";

    private final YiMeiSmsConfig config;

    private final RestTemplate restTemplate;

    public YiMeiSmsChannelSender(YiMeiSmsConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getType() {
        return "YiMei";
    }

    @Override
    public String getName() {
        return "亿美短信渠道";
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity) {

        MultiValueMap<String, Object> param = config.createBaseParam();

        param.add("mobiles", entity.getPhoneNumber());
        param.add("content", entity.getContent());
        param.add("customSmsId", entity.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(param, headers);

        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("对号码为[" + entity.getPhoneNumber() + "]发送短信，参数为:" + param);
            }

            ResponseEntity<String> r = restTemplate.postForEntity(config.getUrl() + DEFAULT_SEND_SMS_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    return RestResult.of(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("对号码为[" + entity.getPhoneNumber() + "]发送短信的响应结果为:" + r.getBody());
                }

                Map<String, Object> data = Casts.readValue(r.getBody(), Map.class);

                if (MapUtils.isEmpty(data)) {
                    return RestResult.of(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE);
                }

                if (data.containsKey(config.getSuccessFieldName()) && data.get(config.getSuccessFieldName()).equals(config.getSuccessFieldValue())) {
                    return new RestResult<>(
                            r.getStatusCode().getReasonPhrase(),
                            r.getStatusCode().value(),
                            String.valueOf(r.getStatusCode().value()),
                            Casts.cast(data.get(config.getResponseDataField())));
                } else {
                    return RestResult.of(
                            "执行返回的[" + config.getSuccessFieldName() + "]值为:" + data.get(config.getSuccessFieldName()) + "，不等于 " + config.getSuccessFieldValue(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE
                    );
                }

            } else {

                return RestResult.of(
                        r.getStatusCode().getReasonPhrase(),
                        r.getStatusCode().value(),
                        String.valueOf(r.getStatusCode().value())
                );

            }
        } catch (Exception e) {
            return RestResult.ofException(e);
        }
    }

    @Override
    public SmsBalanceMeta getBalance() {

        MultiValueMap<String, Object> param = config.createBaseParam();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(param, headers);

        try {
            ResponseEntity<String> r = restTemplate.postForEntity(config.getUrl() + DEFAULT_GET_BALANCE_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    LOGGER.warn("通过 API " + config.getUrl() + DEFAULT_GET_BALANCE_API + " 无任何响应");
                } else {

                    Map<String, Object> data = Casts.readValue(r.getBody(), Map.class);
                    Map<String, Object> balanceMap = Casts.cast(data.get(config.getResponseDataField()));
                    BigDecimal balance = Casts.cast(balanceMap.get(config.getBalanceFieldName()), BigDecimal.class);

                    return new SmsBalanceMeta(getName(), balance);
                }
            } else {
                LOGGER.warn("通过 API " + config.getUrl() + DEFAULT_GET_BALANCE_API + " 获取不到余额信息");
            }

            return null;
        } catch (Exception e) {
            LOGGER.error("通过 API " + config.getUrl() + DEFAULT_GET_BALANCE_API + " 获取余额时，出错", e);

            return null;
        }
    }
}
