package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程审批结果枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyApprovalResultEnum implements NameValueEnum<Integer> {
    /**
     * 已通过
     */
    AGREE(10, "已通过"),

    /**
     * 不通过
     */
    REFUSE(20, "不通过"),
    ;

    private final Integer value;

    private final String name;
}
