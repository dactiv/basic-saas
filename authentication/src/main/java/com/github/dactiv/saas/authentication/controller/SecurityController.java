package com.github.dactiv.saas.authentication.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.security.handler.JsonLogoutSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 授权控制器
 *
 * @author maurice.chen
 */
@RefreshScope
@RestController
public class SecurityController {

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final List<UserDetailsService<?>> userDetailsServices;

    public SecurityController(JsonLogoutSuccessHandler jsonLogoutSuccessHandler,
                              ObjectProvider<UserDetailsService<?>> userDetailsServices) {
        this.jsonLogoutSuccessHandler = jsonLogoutSuccessHandler;
        this.userDetailsServices = userDetailsServices.orderedStream().collect(Collectors.toList());
    }

    /**
     * 登录预处理
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @GetMapping("prepare")
    public RestResult<Map<String, Object>> prepare(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 用户登陆
     *
     * @return 未授权访问结果
     */
    @GetMapping("login")
    public RestResult<Map<String, Object>> login(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 登陆成功后跳转的连接，直接获取当前用户
     *
     * @param securityContext 安全上下文
     * @return 当前用户
     */
    @GetMapping("getPrincipal")
    @PreAuthorize("isAuthenticated()")
    public SecurityUserDetails getPrincipal(@CurrentSecurityContext SecurityContext securityContext) {
        return Casts.cast(securityContext.getAuthentication().getDetails());
    }

    /**
     * 获取系统用户
     *
     * @param userDetails 用户明细
     * @return 系统用户
     */
    @PostMapping("getSystemUser")
    @PreAuthorize("hasRole('BASIC')")
    public SystemUserEntity getSystemUser(@RequestBody BasicUserDetails<Integer> userDetails) {
        Optional<UserDetailsService<?>> optional = userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(userDetails.getUserType()))
                .findFirst();

        if (optional.isEmpty()) {
            return null;
        }

        Object result = optional.get().convertTargetUser(userDetails);

        if (!SystemUserEntity.class.isAssignableFrom(result.getClass())) {
            return null;
        }

        return Casts.cast(result, SystemUserEntity.class);
    }

}
