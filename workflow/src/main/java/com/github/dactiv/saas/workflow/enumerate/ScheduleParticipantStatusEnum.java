package com.github.dactiv.saas.workflow.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日程参与者状态
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ScheduleParticipantStatusEnum implements NameValueEnum<Integer> {

    /**
     * 等待确认
     */
    WAITING(10, "等待确认"),

    /**
     * 已通过
     */
    AGREE(20, "同意参加"),

    /**
     * 不通过
     */
    REFUSE(30, "拒绝参加"),

    ;

    private final Integer value;

    private final String name;
}
