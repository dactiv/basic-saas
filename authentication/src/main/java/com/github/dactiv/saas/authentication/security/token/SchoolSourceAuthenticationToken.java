package com.github.dactiv.saas.authentication.security.token;

import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serial;

/**
 * 学校 app 终端来源认证 token
 *
 * @author maurice.chen
 */
@Getter
public class SchoolSourceAuthenticationToken extends RequestAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -4721046878829151676L;

    private final String code;

    public SchoolSourceAuthenticationToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, UsernamePasswordAuthenticationToken token, String type, String code) {
        super(httpServletRequest, httpServletResponse, token, type, false);
        this.code = code;
    }
}
