package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 通知
     */
    NOTICE(10, "通知"),
    /**
     * @ 我的
     */
    AT_ME(20, "提到我的"),
    /**
     * 警告
     */
    WARNING(30, "系统"),
    /**
     * 系统
     */
    SYSTEM(40, "警告"),

    ;

    /**
     * 值
     */
    private final Integer value;

    /**
     * 名称
     */
    private final String name;

}
