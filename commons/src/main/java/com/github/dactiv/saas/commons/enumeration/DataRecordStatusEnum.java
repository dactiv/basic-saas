package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 数据状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataRecordStatusEnum implements NameValueEnum<Integer> {

    /**
     * 新创建
     */
    NEW(10, "新创建"),

    /**
     * 已更新
     */
    UPDATE(15, "已更新"),

    /**
     * 已发布
     */
    PUBLISH(20, "已发布"),

    ;

    private final Integer value;

    private final String name;

    public static final List<DataRecordStatusEnum> PUBLISH_STATUS = List.of(NEW, UPDATE);
}
