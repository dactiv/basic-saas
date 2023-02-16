package com.github.dactiv.saas.message.service.support.site.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import com.github.dactiv.saas.commons.domain.meta.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.TemplateMessageMeta;
import com.github.dactiv.saas.commons.feign.AuthenticationServiceFeignClient;
import com.github.dactiv.saas.message.service.support.site.WechatMessageTemplateSender;
import com.github.dactiv.saas.message.config.site.SiteConfig;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.saas.message.service.support.site.SiteMessageChannelSender;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 抽象的微信站内信服务实现，用于构造一些基本信息提供给 {@link WechatMessageTemplateSender} 使用。
 *
 * @author maurice.chen
 */
@Component
public class WechatSiteMessageService implements SiteMessageChannelSender {

    private final SiteConfig siteConfig;

    private final List<WechatMessageTemplateSender> messageTemplateSenders;

    private final AuthenticationServiceFeignClient authenticationServiceFeignClient;

    public WechatSiteMessageService(SiteConfig siteConfig,
                                    ObjectProvider<WechatMessageTemplateSender> messageTemplateSenders,
                                    AuthenticationServiceFeignClient authenticationServiceFeignClient) {
        this.siteConfig = siteConfig;
        this.authenticationServiceFeignClient = authenticationServiceFeignClient;
        this.messageTemplateSenders = messageTemplateSenders.orderedStream().collect(Collectors.toList());
    }

    /**
     * 默认类型
     */
    public static final String DEFAULT_TYPE = "wechat_applet";

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public RestResult<Map<String, Object>> sendSiteMessage(SiteMessageEntity message) {
        BasicUserDetails<Integer> userDetails = BasicUserDetails.of(
                message.getUserId(),
                message.getUsername(),
                message.getUserType()
        );

        Map<String, Object> map = authenticationServiceFeignClient.getSystemUser(userDetails);

        if (!map.containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_KEY)) {
            return RestResult.of(
                    "ID 为 [" + message.getUserId() + "] 类型为 [" + message.getUserType() + "] 的用户未绑定微信，无需推送消息。",
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase()
            );
        }

        List<Object> ids = siteConfig
                .getWechat()
                .getSupportIdFieldName()
                .stream().map(s -> message.getMeta().get(s))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            return RestResult.of(
                    "找不到 " + siteConfig.getWechat().getSupportIdFieldName() + " 信息，无需推送消息。",
                    HttpStatus.NOT_FOUND.value(),
                    String.valueOf(HttpStatus.NOT_FOUND.value())
            );
        }

        WechatUserDetails wechatUserDetails = Casts.convertValue(map.get(SecurityUserDetailsConstants.SECURITY_DETAILS_WECHAT_KEY), SimpleWechatUserDetailsMeta.class);
        List<WechatMessageTemplateSender> senders = messageTemplateSenders
                .stream()
                .filter(s -> ids.stream().anyMatch(id -> s.isSupport(id.toString())))
                .collect(Collectors.toList());

        Map<String, RestResult<Map<String, Object>>> restResults = new LinkedHashMap<>();

        for (WechatMessageTemplateSender sender : senders) {
            try {
                TemplateMessageMeta meta = sender.createTemplateMessageMeta(message, wechatUserDetails);
                sender.sendMessage(meta);
                restResults.put(sender.getName(), RestResult.of("发送 ID 为 [" + sender.getName() + "] 的消息到目标用户 [" + wechatUserDetails.getOpenId() + "]:" + userDetails.getUsername() + " 成功"));
            } catch (Exception e) {
                restResults.put(sender.getName(), RestResult.ofException(e));
            }
        }

        List<String> resultMessage = restResults
                .values()
                .stream()
                .map(RestResult::getMessage)
                .collect(Collectors.toList());

        int status = HttpStatus.OK.value();
        String executeCode = RestResult.SUCCESS_EXECUTE_CODE;

        if (CollectionUtils.isEmpty(resultMessage)) {
            status = HttpStatus.NOT_FOUND.value();
            executeCode = String.valueOf(HttpStatus.NOT_FOUND.value());
        } else if (restResults.values().stream().anyMatch(r -> r.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value())) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            executeCode = RestResult.FAIL_EXECUTE_CODE;
        }

        return RestResult.of(StringUtils.join(resultMessage, SpringMvcUtils.COMMA_STRING), status, executeCode);
    }

}
