package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 自定义申请类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApplyFormTypeEnum implements NameValueEnum<Integer> {

    /**
     * 自定义格式表单
     */
    CUSTOM(10, "自定义格式表单"),

    /**
     * 系统默认表单
     */
    SYSTEM(15, "系统默认表单"),

    /**
     * 课程格式表单
     */
    CURRICULUM(20, "课程格式表单"),

    /**
     * 章节课时内容格式表单
     */
    CHAPTER_CLASS_HOUR_CONTENT(30, "章节课时内容格式表单"),

    /**
     * 课程考试内容格式表单
     */
    CURRICULUM_EXAMINATION(40, "课程考试内容格式表单"),

    /**
     * 章节测验内容格式表单
     */
    CHAPTER_TEST(50, "章节测验内容格式表单"),

    /**
     * 章节作业内容格式表单
     */
    CHAPTER_HOMEWORK(60, "章节作业内容格式表单"),
    ;

    private final Integer value;

    private final String name;

    public static final List<ApplyFormTypeEnum> SYSTEM_TYPE = List.of(CUSTOM, SYSTEM);
}
