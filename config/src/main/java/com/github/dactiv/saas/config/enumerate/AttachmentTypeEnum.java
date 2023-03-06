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
