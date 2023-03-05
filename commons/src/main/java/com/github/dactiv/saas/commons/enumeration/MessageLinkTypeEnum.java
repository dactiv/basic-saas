package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息链接类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageLinkTypeEnum implements NameValueEnum<String> {

    /**
     * 工作流申请
     */
    WORKFLOW_APPLY("工作流程申请", "workflow.apply"),

    /**
     * 认证信息
     */
    AUTHENTICATION_INFO("认证信息", "authentication.info"),
    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 值
     */
    private final String value;

}
