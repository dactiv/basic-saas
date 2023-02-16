package com.github.dactiv.saas.authentication.service;

import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.commons.domain.WechatUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import java.util.Objects;

/**
 * 微信认证服务
 *
 * @author maurice.chen
 */
public interface WechatAuthenticationService<T extends WechatUserDetails> {

    /**
     * 获取微信用户明细
     *
     * @param phoneNumber 手机号码
     * @return 微信用户明细
     */
    T getByPhoneNumber(String phoneNumber);

    /**
     * 获取微信用户明细
     *
     * @param openId 微信 open id
     * @return 微信用户明细
     */
    T getByWechatOpenId(String openId);

    /**
     * 更新实体
     *
     * @param entity 实体内容
     * @return 执行结果
     */
    int updateById(T entity);

    /**
     * 获取微信用户明细
     *
     * @param token 微信认证 token
     * @return 微信用户明细
     */
    default T getByWechatAuthenticationToken(WechatAuthenticationToken token) {
        T result = null;

        if (Objects.nonNull(token.getPhoneInfo())) {
            result = getByPhoneNumber(token.getPhoneInfo().getPurePhoneNumber());
        }

        if (Objects.isNull(result)) {
            result = getByWechatOpenId(token.getUserDetails().getOpenId());
        }

        return result;
    }

    /**
     * 绑定微信用户明细
     *
     * @param wechatUserDetails  微信用户明细
     * @param currentUserDetails 当前用户明细
     */
    default void buildWechatUserDetails(WechatUserDetails wechatUserDetails, T currentUserDetails) throws AuthenticationException {
        if (StringUtils.isEmpty(currentUserDetails.getOpenId())) {
            currentUserDetails.setOpenId(wechatUserDetails.getOpenId());
        } else if (!StringUtils.equals(currentUserDetails.getOpenId(), wechatUserDetails.getOpenId())) {
            throw new AuthenticationServiceException("当前用户已绑定其他微信号，请解绑后在重新绑定");
        }

        if (StringUtils.isEmpty(currentUserDetails.getUnionId())) {
            currentUserDetails.setUnionId(wechatUserDetails.getUnionId());
        } else if (!StringUtils.equals(currentUserDetails.getUnionId(), wechatUserDetails.getUnionId())) {
            throw new AuthenticationServiceException("当前用户已绑定其他微信号，请解绑后在重新绑定");
        }

        currentUserDetails.setSessionKey(wechatUserDetails.getSessionKey());

        updateById(currentUserDetails);
    }

    default void updateWechatSessionKey(T entity, String sessionKey) {
        if (StringUtils.equals(entity.getSessionKey(), sessionKey)) {
            return;
        }

        entity.setSessionKey(sessionKey);
        updateById(entity);
    }
}
