package com.github.dactiv.saas.message.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 通知类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NoticeMessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 站内信
     */
    NOTICE("公告", 10),
    /**
     * 邮件
     */
    NEWS("新闻", 20),

    ;

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final Integer value;

}
