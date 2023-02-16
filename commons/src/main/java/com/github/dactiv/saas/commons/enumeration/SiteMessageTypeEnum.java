package com.github.dactiv.saas.commons.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 站内信类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SiteMessageTypeEnum {

    /**
     * 通知
     */
    MESSAGE(10, "消息通知"),

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
