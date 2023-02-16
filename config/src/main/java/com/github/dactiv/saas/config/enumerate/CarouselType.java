package com.github.dactiv.saas.config.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轮播图类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CarouselType implements NameValueEnum<Integer> {

    /**
     * 电脑端
     */
    PC(10, "电脑端"),
    /**
     * APP
     */
    APP(20, "APP"),
    /**
     * 小程序
     */
    APPLET(30, "小程序");

    private final Integer value;

    private final String name;
}
