package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.ConsoleUserService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 系统用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
public class ConsoleUserDetailsService extends AbstractUserDetailsService<ConsoleUserEntity> {

    private final AuthorizationService authorizationService;

    private final ConsoleUserService consoleUserService;

    private final PasswordEncoder passwordEncoder;

    public ConsoleUserDetailsService(AuthorizationService authorizationService,
                                     ConsoleUserService consoleUserService,
                                     PasswordEncoder passwordEncoder,
                                     AuthenticationProperties properties) {
        super(properties);
        this.authorizationService = authorizationService;
        this.consoleUserService = consoleUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        ConsoleUserEntity user = consoleUserService.getByIdentity(token.getPrincipal().toString());

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        SecurityUserDetails userDetails = new SecurityUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus()
        );

        authorizationService.setSystemUserAuthorities(user, userDetails);

        userDetails.setMeta(user.toSecurityUserDetailsMeta());

        return userDetails;
    }

    @Override
    public CacheProperties getAuthorizationCache(PrincipalAuthenticationToken token) {
        return CacheProperties.of(
                "dactiv:saas:" + DEFAULT_AUTHORIZATION_KEY_NAME + token.getType() + ":" + token.getPrincipal(),
                TimeProperties.of(7, TimeUnit.DAYS)
        );
    }

    @Override
    public CacheProperties getAuthenticationCache(PrincipalAuthenticationToken token) {
        return CacheProperties.of(
                "dactiv:saas:" + DEFAULT_AUTHENTICATION_KEY_NAME + token.getType() + ":" + token.getPrincipal(),
                new TimeProperties(7, TimeUnit.DAYS)
        );
    }

    @Override
    public List<String> getType() {
        return List.of(ResourceSourceEnum.CONSOLE_SOURCE_VALUE);
    }

    @Override
    public PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails, PrincipalAuthenticationToken token, Collection<? extends GrantedAuthority> grantedAuthorities) {

        ResourceSourceEnum sourceEnum = ResourceSourceEnum.of(token.getType());
        Assert.isTrue(Objects.nonNull(sourceEnum), "找不到枚举值为 [" + token.getType() + "] 的资源来源类型");

        return new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials()),
                sourceEnum.toString(),
                userDetails,
                grantedAuthorities,
                false
        );
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public ConsoleUserEntity convertTargetUser(SecurityUserDetails userDetails) {
        return consoleUserService.get(Casts.cast(userDetails.getId(), Integer.class));
    }

    @Override
    public ConsoleUserEntity convertTargetUser(TypeUserDetails<?> userDetails) {
        return consoleUserService.get(Casts.cast(userDetails.getUserId(), Integer.class));
    }

    @Override
    public void updatePassword(ConsoleUserEntity consoleUser, String oldPassword, String newPassword) {

        if (!getPasswordEncoder().matches(oldPassword, consoleUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        ConsoleUserEntity user = consoleUser.ofIdData();
        user.setPassword(passwordEncoder.encode(newPassword));
        consoleUserService.updateById(user);
    }
}
