package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件来源类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileFromTypeEnum implements NameValueEnum<String> {
    /**
     * minio 文件来源
     */
    MINIO("minio", "minio 文件来源"),
    ;

    private final String value;

    private final String name;
}
