package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单参与者类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FormParticipantTypeEnum implements NameValueEnum<Integer> {

    /**
     * 审批人
     */
    APPROVER(10, "审批人"),

    /**
     * 抄送人
     */
    COPY(20, "抄送人"),

    ;

    private final Integer value;

    private final String name;
}
