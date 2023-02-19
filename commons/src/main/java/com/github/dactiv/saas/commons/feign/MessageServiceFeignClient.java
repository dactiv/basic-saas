package com.github.dactiv.saas.commons.feign;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.enumeration.MessageTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_MESSAGE_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface MessageServiceFeignClient {

    /**
     * 默认的消息类型 key 名称
     */
    String DEFAULT_MESSAGE_TYPE_KEY = "messageType";

    String DEFAULT_MESSAGES_KEY = "messages";

    String DEFAULT_SITE_TYPE_VALUE = "site";

    String DEFAULT_SMS_TYPE_VALUE = "sms";

    String DEFAULT_EMAIL_TYPE_VALUE = "email";

    String DEFAULT_DATE_META_KEY = "meta";

    String DEFAULT_TITLE_KEY = "title";

    String DEFAULT_CONTENT_KEY = "content";

    String CURRICULUM_FORUM_COMMENT_NOTICE_MESSAGE_QUEUE_NAME = "dactiv.saas.resources.curriculum.forum.comment";

    String CURRICULUM_FORUM_COMMENT_DELETE_NOTICE_MESSAGE_QUEUE_NAME = "dactiv.saas.resources.curriculum.forum.comment.delete";

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    @PostMapping("send")
    RestResult<Object> send(@RequestBody Map<String, Object> request);

    /**
     * 删除附件信息
     *
     * @param id         主键 id
     * @param type       消息类型
     * @param fileObject 文件对象
     */
    @PostMapping("removeAttachment")
    RestResult<Object> removeAttachment(@RequestParam("id") Integer id, @RequestParam("type") String type, @RequestBody FileObject fileObject);

    /**
     * 常量类
     */
    class Constants {

        public static final String TYPE_FIELD = "type";

        /**
         * 站内信常量
         */
        public static class Site {

            public static final String TO_USERS_FIELD = "toUsers";

            public static final String IS_PUSHABLE_FIELD = "pushable";

            public static final String LINKE_META_FIELD = "link";

        }

        public static class Sms {
            public static final String PHONE_NUMBERS_FIELD = "phoneNumbers";
        }
    }

    static Map<String, Object> createNoticeSiteMessage(List<TypeIdNameMeta> toUsers, String title, String content) {
        return createNoticeSiteMessage(toUsers, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createNoticeSiteMessage(List<TypeIdNameMeta> toUsers, String title, String content, Map<String, ?> meta) {
        return createSiteMessage(toUsers, MessageTypeEnum.NOTICE, title, content, meta);
    }

    static Map<String, Object> createSiteMessage(List<TypeIdNameMeta> toUsers, MessageTypeEnum messageType, String title, String content) {
        return createSiteMessage(toUsers, messageType, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createSiteMessage(List<TypeIdNameMeta> toUsers, MessageTypeEnum messageType, String title, String content, Map<String, ?> meta) {
        return createSiteMessage(toUsers, messageType, YesOrNo.No, title, content, meta);
    }

    static Map<String, Object> createPushableNoticeSiteMessage(List<TypeIdNameMeta> toUsers, String title, String content) {
        return createPushableNoticeSiteMessage(toUsers, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createPushableNoticeSiteMessage(List<TypeIdNameMeta> toUsers, String title, String content, Map<String, ?> meta) {
        return createPushableSiteMessage(toUsers, MessageTypeEnum.NOTICE, title, content, meta);
    }

    static Map<String, Object> createPushableSiteMessage(List<TypeIdNameMeta> toUsers, MessageTypeEnum messageType, String title, String content) {
        return createPushableSiteMessage(toUsers, messageType, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createPushableSiteMessage(List<TypeIdNameMeta> toUsers, MessageTypeEnum messageType, String title, String content, Map<String, ?> meta) {
        return createSiteMessage(toUsers, messageType, YesOrNo.Yes, title, content, meta);
    }

    static Map<String, Object> createSiteMessage(List<TypeIdNameMeta> toUsers, MessageTypeEnum messageType, YesOrNo pushable, String title, String content, Map<String, ?> meta) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put(MessageServiceFeignClient.Constants.Site.TO_USERS_FIELD, toUsers);
        map.put(MessageServiceFeignClient.Constants.TYPE_FIELD, messageType.getValue());
        map.put(MessageServiceFeignClient.Constants.Site.IS_PUSHABLE_FIELD, pushable);

        map.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_SITE_TYPE_VALUE);

        map.put(MessageServiceFeignClient.DEFAULT_TITLE_KEY, title);
        map.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, content);

        map.put(MessageServiceFeignClient.DEFAULT_DATE_META_KEY, meta);

        return map;
    }

}
