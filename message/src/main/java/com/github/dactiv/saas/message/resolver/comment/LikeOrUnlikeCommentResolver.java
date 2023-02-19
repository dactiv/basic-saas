package com.github.dactiv.saas.message.resolver.comment;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;
import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;
import com.github.dactiv.saas.message.enumerate.LikeOrUnlikeTargetTypeEnum;
import com.github.dactiv.saas.message.resolver.LikeOrUnlikeResolver;
import com.github.dactiv.saas.message.service.CommentMessageService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 评论点赞或非点赞解析器实现
 *
 * @author maurice.chen
 */
@Component
public class LikeOrUnlikeCommentResolver implements LikeOrUnlikeResolver {

    private final CommentMessageService commentMessageService;

    public LikeOrUnlikeCommentResolver(CommentMessageService commentMessageService) {
        this.commentMessageService = commentMessageService;
    }

    @Override
    public boolean isSupport(LikeOrUnlikeTargetTypeEnum type) {
        return LikeOrUnlikeTargetTypeEnum.COMMENT.equals(type);
    }

    @Override
    public Map<String, Object> postSave(LikeOrUnlikeEntity likeOrUnlike) {
        CommentMessageEntity entity = commentMessageService.get(likeOrUnlike.getTargetId());
        if (YesOrNo.Yes.equals(likeOrUnlike.getIsLike())) {
            entity.setLikeCount(entity.getLikeCount() + 1);
        } else {
            entity.setUnlikeCount(entity.getUnlikeCount() + 1);
        }
        commentMessageService.updateById(entity);
        return LikeOrUnlikeResolver.super.postSave(likeOrUnlike);
    }

    @Override
    public Map<String, Object> postDelete(LikeOrUnlikeEntity likeOrUnlike) {
        CommentMessageEntity entity = commentMessageService.get(likeOrUnlike.getTargetId());
        if (YesOrNo.Yes.equals(likeOrUnlike.getIsLike())) {
            entity.setLikeCount(entity.getLikeCount() - 1);
        } else {
            entity.setUnlikeCount(entity.getUnlikeCount() - 1);
        }
        commentMessageService.updateById(entity);
        return LikeOrUnlikeResolver.super.postDelete(likeOrUnlike);
    }
}
