package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息链接类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageLinkTypeEnum implements NameValueEnum<String> {

    /**
     * 工作流申请
     */
    WORKFLOW_APPLY("工作流程申请", "workflow.apply"),
    /**
     * 课程章节作业
     */
    CURRICULUM_CHAPTER_HOMEWORK("章节作业", "resources.curriculum.chapter.homework"),
    /**
     * 课程章节测试
     */
    CURRICULUM_CHAPTER_TEST("章节测试", "resources.curriculum.chapter.test"),
    /**
     * 课程章节测试
     */
    CURRICULUM_EXAMINATION("章节考试", "resources.curriculum.examination"),

    AUTHENTICATION_INFO("认证信息", "authentication.info"),
    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 值
     */
    private final String value;

}
