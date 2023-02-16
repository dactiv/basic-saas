package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计操作类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuditOperationTypeEnum implements NameValueEnum<Integer> {

    /**
     * 审核
     */
    AUDIT(10, "审核"),
    /**
     * 撤销
     */
    CANCEL(20, "撤销"),
    ;

    private final Integer value;

    private final String name;
}
