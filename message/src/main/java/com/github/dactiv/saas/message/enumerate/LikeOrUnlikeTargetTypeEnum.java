package com.github.dactiv.saas.message.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LikeOrUnlikeTargetTypeEnum implements NameValueEnum<Integer> {

    /**
     * 课程评价
     */
    COMMENT("评论", 10),

    ;

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final Integer value;
}
