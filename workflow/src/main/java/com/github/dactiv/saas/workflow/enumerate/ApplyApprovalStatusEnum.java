package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 流程审批状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyApprovalStatusEnum implements NameValueEnum<Integer> {

    /**
     * 等待审批
     */
    WAITING(10, "待审批"),

    /**
     * 执行审批
     */
    PROCESSING(20, "正在审批"),

    /**
     * 审批完成
     */
    COMPLETE(30, "审批完成"),
    /**
     * 无需审批
     */
    ABSTAIN(40, "无需审批"),
    ;

    private final Integer value;

    private final String name;

    public static final List<Integer> CANCEL_APPLY_STATUS = List.of(WAITING.value, PROCESSING.value);
}
