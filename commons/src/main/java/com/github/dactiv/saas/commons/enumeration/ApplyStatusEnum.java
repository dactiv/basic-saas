package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 流程申请状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyStatusEnum implements NameValueEnum<Integer> {

    /**
     * 未提交
     */
    NEW(10, "未提交"),

    /**
     * 审批中
     */
    EXECUTING(20, "审批中"),

    /**
     * 已通过
     */
    AGREE(30, "已通过"),

    /**
     * 不通过
     */
    REFUSE(40, "不通过"),

    /**
     * 已撤销
     */
    CANCEL(50, "已撤销"),

    ;

    private final Integer value;

    private final String name;

    public final static List<ApplyStatusEnum> SCHEDULE_STATUS = List.of(AGREE, REFUSE);

    public final static List<ApplyStatusEnum> DELETE_STATUS = List.of(NEW, EXECUTING);

    public final static List<ApplyStatusEnum> SUBMIT_STATUS = List.of(NEW, CANCEL, REFUSE);

}
