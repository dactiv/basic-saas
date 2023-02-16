package com.github.dactiv.saas.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀请方式
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InviteModeEnum implements NameValueEnum<Integer> {

    /**
     * 链接邀请
     */
    URL(10, "链接邀请"),
    /**
     * 筛选邀请
     */
    SCREEN(20, "筛选邀请");

    private final Integer value;

    private final String name;
}
