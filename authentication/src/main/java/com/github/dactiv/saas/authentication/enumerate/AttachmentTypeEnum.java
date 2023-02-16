package com.github.dactiv.saas.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentTypeEnum implements NameValueEnum<String> {

    /**
     * 基础服务附件
     */
    BASIC("basic", "基础服务附件"),
    /**
     * 消息服务附件
     */
    MESSAGE("message", "消息服务附件"),
    /**
     * 工作流服务附件
     */
    WORKFLOW("workflow", "工作流服务附件"),
    /**
     * 教师服务附件
     */
    TEACHER("teacher", "教师服务附件"),
    /**
     * 头像附件
     */
    AVATAR("avatar", "头像附件"),
    ;

    private final String value;

    private final String name;
}
