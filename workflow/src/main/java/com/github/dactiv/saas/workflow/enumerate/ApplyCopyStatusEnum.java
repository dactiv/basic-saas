package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程审批状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyCopyStatusEnum implements NameValueEnum<Integer> {

    /**
     * 待抄送
     */
    WAITING(10, "待抄送"),

    /**
     * 已抄送
     */
    PROCESSED(20, "已抄送"),
    /**
     * 无需抄送
     */
    ABSTAIN(30, "无需抄送"),
    ;

    private final Integer value;

    private final String name;
}
