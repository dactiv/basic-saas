package com.github.dactiv.saas.authentication.security;

/**
 * 访问 token 接口
 *
 * @author maurice.chen
 */
public interface UserDetailsAccessToken {

    /**
     * 获取访问 token
     *
     * @return token
     */
    String getAccessToken();
}
