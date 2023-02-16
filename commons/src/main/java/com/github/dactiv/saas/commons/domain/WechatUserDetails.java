package com.github.dactiv.saas.commons.domain;

/**
 * 微信用户明细
 *
 * @author maurice.chen
 */
public interface WechatUserDetails {

    /**
     * 获取 session key
     *
     * @return session key
     */
    String getSessionKey();

    /**
     * 获取用户在开放平台的唯一标识符
     *
     * @return 开放平台的唯一标识符
     */
    String getUnionId();

    /**
     * 获取用户唯一标识
     *
     * @return 用户唯一标识
     */
    String getOpenId();

    /**
     * 设置 session key
     *
     * @param sessionKey session key
     */
    void setSessionKey(String sessionKey);

    /**
     * 设置用户在开放平台的唯一标识符
     *
     * @param unionId 用户在开放平台的唯一标识符
     */
    void setUnionId(String unionId);

    /**
     * 设置用户唯一标识
     *
     * @param openId 用户唯一标识
     */
    void setOpenId(String openId);

}
