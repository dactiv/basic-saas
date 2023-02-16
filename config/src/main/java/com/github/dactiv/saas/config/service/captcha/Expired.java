package com.github.dactiv.saas.config.service.captcha;

/**
 * 可过期的数据实体接口
 *
 * @author maurice
 */
public interface Expired {

    /**
     * 是否过期
     *
     * @return true 是，否则 false
     */
    boolean isExpired();
}
