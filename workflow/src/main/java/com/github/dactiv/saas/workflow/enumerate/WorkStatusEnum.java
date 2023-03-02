package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作表状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WorkStatusEnum implements NameValueEnum<Integer> {

    /**
     * 待处理
     */
    PROCESSING(10, "待处理"),

    /**
     * 已处理
     */
    PROCESSED(20, "已处理"),

    /**
     * 已撤销
     */
    CANCEL(30, "已撤销"),

    ;

    private final Integer value;

    private final String name;
}
