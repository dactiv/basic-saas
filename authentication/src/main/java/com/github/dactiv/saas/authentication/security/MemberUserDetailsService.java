package com.github.dactiv.saas.authentication.security;

import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.security.token.WechatAuthenticationToken;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.commons.domain.meta.wechat.SimpleWechatUserDetailsMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.service.WechatService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会员用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
public class MemberUserDetailsService extends MobileUserDetailService{

    public MemberUserDetailsService(ApplicationConfig applicationConfig,
                                    PasswordEncoder passwordEncoder,
                                    AuthorizationService authorizationService,
                                    AuthenticationProperties authenticationProperties,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    WechatService wechatService) {
        super(
                applicationConfig,
                passwordEncoder,
                authorizationService,
                authenticationProperties,
                deviceIdContextRepository,
                wechatService
        );
    }

    @Override
    public List<String> getMobileType() {
        return List.of(ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE);
    }

    @Override
    public List<String> getWechatType() {
        return List.of(ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE);
    }

    @Override
    protected void buildWechatUserDetailsMeta(SimpleWechatUserDetailsMeta meta, PrincipalAuthenticationToken result) {

    }

    @Override
    protected void updateWechatSessionKey(SecurityUserDetails userDetails, WechatAuthenticationToken token) {

    }

    @Override
    protected SystemUserEntity getBasicAuthenticationSystemUser(RequestAuthenticationToken token) {
        return null;
    }

    @Override
    protected SystemUserEntity getWechatTypeSystemUser(RequestAuthenticationToken token) {
        return null;
    }

    @Override
    public List<String> getType() {
        return null;
    }
}
