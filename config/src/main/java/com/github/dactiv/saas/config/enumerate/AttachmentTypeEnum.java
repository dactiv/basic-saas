package com.github.dactiv.saas.config.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentTypeEnum implements NameValueEnum<String> {

    /**
     * 基础服务附件
     */
    BASIC("basic", "基础服务附件"),
    /**
     * 消息服务附件
     */
    MESSAGE("message", "消息服务附件"),
    /**
     * 工作流服务附件
     */
    WORKFLOW("workflow", "工作流服务附件"),
    /**
     * 教师服务附件
     */
    TEACHER("teacher", "教师服务附件"),
    /**
     * 课件附件
     */
    USER_FILE("user.file", "用户资源附件"),
    /**
     * 头像附件
     */
    AVATAR("avatar", "头像附件"),
    /**
     * 站点动态封面
     */
    NOTICE_COVER("notice.cover", "站点动态封面附件"),
    /**
     * 课程封面
     */
    CURRICULUM_COVER("curriculum.cover", "课程封面附件"),
    /**
     * 用户章节作业附件
     */
    USER_CHAPTER_HOMEWORK("user.chapter.homework","用户章节作业附件"),
    /**
     * 考试主观题附件
     */
    EXAMINATION_SUBJECTIVE_QUESTION("examination.subjective.question","考试主观题附件"),
    /**
     * 课程公告附件
     */
    CURRICULUM_NOTICE("curriculum.notice", "课程公告附件"),
    /**
     * 富文本附件
     */
    RICH_TEXT_ATTACHMENT("rich.text","富文本附件"),
    /**
     * 主观题题目附件
     */
    SUBJECTIVE_QUESTION_ATTACHMENT("subjective.question", "主观题题目附件"),
    /**
     * 课程章节课时内容
     */
    CURRICULUM_CHAPTER_CLASS_HOUR_CONTENT("curriculum.chapter.class.hour.content", "课程章节课时内容附件"),
    /**
     * 轮播图封面
     */
    CAROUSEL("carousel.cover", "轮播图封面"),
    /**
     * 临时文件
     */
    TEMP("temp", "临时文件附件"),
    ;

    private final String value;

    private final String name;
}
