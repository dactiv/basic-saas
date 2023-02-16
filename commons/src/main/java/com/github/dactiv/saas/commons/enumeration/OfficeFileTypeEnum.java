package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * office 文件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OfficeFileTypeEnum implements NameValueEnum<List<String>> {

    /**
     * Word 文档
     */
    WORD(List.of("doc", "docx"), "Word 文档", "KWPS.Application", "Documents"),
    /**
     * Excel 文档
     */
    EXCEL(List.of("xls", "xlsx"), "Excel 文档", "KET.Application", "Workbooks"),
    /**
     * PowerPoint 文档
     */
    POWER_POINT(List.of("ppt", "pptx"), "PowerPoint 文档", "KWPP.Application", "Presentations"),

    ;

    private final List<String> value;

    private final String name;

    private final String activeXComponentName;

    private final String property;

    public static OfficeFileTypeEnum pare(String file) {
        return Arrays
                .stream(OfficeFileTypeEnum.values())
                .filter(e -> e.getValue().stream().anyMatch(s -> StringUtils.endsWith(file, s)))
                .findFirst()
                .orElse(null);
    }
}
