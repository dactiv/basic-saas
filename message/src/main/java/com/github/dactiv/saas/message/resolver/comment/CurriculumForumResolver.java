package com.github.dactiv.saas.message.resolver.comment;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.dto.UserTypeIdNameDto;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;
import com.github.dactiv.saas.message.resolver.CommentMessageResolver;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 课程讨论区回复消息解析器实现
 *
 * @author maurice.chen
 */
@Component
public class CurriculumForumResolver implements CommentMessageResolver {

    public static final String CURRICULUM_ID_FIELD_NAME = "curriculumId";

    public static final String CURRICULUM_FORUM_ID_FIELD_NAME = "curriculumForumId";

    public static final List<String> SUPPORT_TYPES = List.of("CURRICULUM_FORUM_ISSUES", "CURRICULUM_FORUM_COMMUNICATION", "CURRICULUM_FORUM_CUSTOM");

    @Override
    public boolean isSupport(String type) {
        return SUPPORT_TYPES.contains(type);
    }

    @Override
    public boolean preSave(CommentMessageEntity entity) {
        Assert.isTrue(entity.getMeta().containsKey(CURRICULUM_ID_FIELD_NAME), "实体的 meta 字段里的 [" + CURRICULUM_ID_FIELD_NAME + "] 不能为空");
        Assert.isTrue(entity.getMeta().containsKey(CURRICULUM_FORUM_ID_FIELD_NAME), "实体的 meta 字段里的 [" + CURRICULUM_FORUM_ID_FIELD_NAME + "] 不能为空");
        return CommentMessageResolver.super.preSave(entity);
    }

    @Override
    public Map<String, Object> postSave(CommentMessageEntity entity) {
        Object close = entity.getMeta().get(CommentMessageEntity.CLOSE_META_KEY);
        YesOrNo type = YesOrNo.No;
        if (Objects.nonNull(close)) {
            type = BooleanUtils.toBoolean(close.toString()) ? YesOrNo.Yes : YesOrNo.No;
        }
        TypeIdNameMeta meta = TypeIdNameMeta.of(entity.getTargetId(), entity.getTargetName(), type.toString());
        //noinspection unchecked
        UserTypeIdNameDto<Integer> body = Casts.of(meta, UserTypeIdNameDto.class);
        body.setUserDetails(BasicUserDetails.of(entity.getUserId(), entity.getUsername(), entity.getUserType()));
        return Map.of(
                SystemConstants.NOTICE_MESSAGE_QUEUE_NAME, MessageServiceFeignClient.CURRICULUM_FORUM_COMMENT_NOTICE_MESSAGE_QUEUE_NAME,
                SystemConstants.NOTICE_MESSAGE_EXCHANGE_NAME, SystemConstants.SYS_RESOURCES_RABBITMQ_EXCHANGE,
                SystemConstants.NOTICE_MESSAGE_BODY_NAME, body
        );
    }

    @Override
    public Map<String, Object> postDelete(CommentMessageEntity entity) {
        IdNameMeta meta = IdNameMeta.of(entity.getTargetId(), entity.getTargetName());
        return Map.of(
                SystemConstants.NOTICE_MESSAGE_QUEUE_NAME, MessageServiceFeignClient.CURRICULUM_FORUM_COMMENT_DELETE_NOTICE_MESSAGE_QUEUE_NAME,
                SystemConstants.NOTICE_MESSAGE_EXCHANGE_NAME, SystemConstants.SYS_RESOURCES_RABBITMQ_EXCHANGE,
                SystemConstants.NOTICE_MESSAGE_BODY_NAME, meta
        );
    }
}
