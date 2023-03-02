package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作类型枚举枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WorkTypeEnum implements NameValueEnum<Integer> {

    /**
     * 我发起的
     */
    CREATED(10, "我发起的"),

    /**
     * 我的代办
     */
    PENDING(20, "我的代办"),

    /**
     * 我的经办
     */
    PROCESSED(30, "我的经办"),
    /**
     *
     */
    COPY(40, "我收到的")

    ;

    private final Integer value;

    private final String name;
}
