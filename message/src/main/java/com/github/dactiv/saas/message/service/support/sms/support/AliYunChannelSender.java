package com.github.dactiv.saas.message.service.support.sms.support;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.saas.message.config.sms.AliYunSmsConfig;
import com.github.dactiv.saas.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.saas.message.domain.meta.SmsBalanceMeta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 阿里云渠道短信发送者实现
 *
 * @author maurice.chen
 */
@Component
public class AliYunChannelSender implements SmsChannelSender {

    private final Client client;

    public AliYunChannelSender(AliYunSmsConfig aliyunSmsConfig) throws Exception {
        Config config = new Config();

        config.accessKeyId = aliyunSmsConfig.getAccessKeyId();
        config.accessKeySecret = aliyunSmsConfig.getAccessKeySecret();
        config.endpoint = aliyunSmsConfig.getEndpoint();

        client = new Client(config);
    }

    @Override
    public String getType() {
        return "AliYun";
    }

    @Override
    public String getName() {
        return "阿里云短信渠道";
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity) {
        SendSmsRequest sendSmsRequest = Casts.of(entity.getAliYunMeta(), SendSmsRequest.class);

        sendSmsRequest.phoneNumbers = entity.getPhoneNumber();

        try {
            SendSmsResponse response = client.sendSms(sendSmsRequest);
            //noinspection unchecked
            Map<String, Object> bodyMap = Casts.convertValue(response.getBody(), Map.class);
            if (HttpStatus.OK.getReasonPhrase().equals(response.body.code)) {
                return RestResult.ofSuccess(response.body.message, bodyMap);
            } else {
                return RestResult.of(response.body.message, HttpStatus.INTERNAL_SERVER_ERROR.value(), response.body.getCode(), bodyMap);
            }
        } catch (Exception e) {
            return RestResult.ofException(e);
        }
    }

    @Override
    public SmsBalanceMeta getBalance() {
        return null;
    }

}
