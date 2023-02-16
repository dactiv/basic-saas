package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入导出类型美剧
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImportExportTypeEnum implements NameValueEnum<String> {

    /**
     * 题库类型
     */
    QUESTION_BANK("question-bank", "题库类型"),


    ;

    private final String value;

    private final String name;
}
