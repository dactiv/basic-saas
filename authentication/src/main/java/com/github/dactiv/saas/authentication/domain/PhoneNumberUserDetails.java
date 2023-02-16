package com.github.dactiv.saas.authentication.domain;

/**
 * 带手机号码的用户信息
 *
 * @author maurice.chen
 */
public interface PhoneNumberUserDetails {

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    String getPhoneNumber();
}
