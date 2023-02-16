package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单审批类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FormApprovalTypeEnum implements NameValueEnum<Integer> {

    /**
     * 审批人
     */
    SEQUENCE(10, "顺序审批"),

    /**
     * 会签
     */
    CONSENSUS(20, "会签审批"),

    /**
     * 或签
     */
    UNANIMOUS(30, "或签审批"),

    ;

    private final Integer value;

    private final String name;
}
