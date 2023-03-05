package com.github.dactiv.saas.config.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预览文件类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PreviewFileTypeEnum implements NameValueEnum<Integer> {

    /**
     * 用户上传资源文档
     */
    USER_FILE_DOC("用户上传资源文档", 10),
    ;

    private final String name;

    private final Integer value;
}
