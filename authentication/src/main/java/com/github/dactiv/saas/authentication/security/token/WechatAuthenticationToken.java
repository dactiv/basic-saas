package com.github.dactiv.saas.authentication.security.token;

import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.saas.commons.domain.meta.wechat.PhoneInfoMeta;
import com.github.dactiv.saas.commons.domain.meta.wechat.SimpleWechatUserDetailsMeta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serial;

/**
 * 微信认证 token
 *
 * @author maurice.chen
 */
@Getter
public class WechatAuthenticationToken extends RequestAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -718127800025123393L;

    /**
     * 微信用户信息
     */
    private final SimpleWechatUserDetailsMeta userDetails;

    /**
     * 电话信息
     */
    @Setter
    private PhoneInfoMeta phoneInfo;

    public WechatAuthenticationToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String type, SimpleWechatUserDetailsMeta userDetails) {
        super(httpServletRequest, httpServletResponse, new UsernamePasswordAuthenticationToken(userDetails.getOpenId(), userDetails.getSessionKey()), type, false);
        this.userDetails = userDetails;
    }
}
