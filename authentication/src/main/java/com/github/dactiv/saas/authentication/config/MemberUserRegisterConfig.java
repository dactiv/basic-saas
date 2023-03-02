package com.github.dactiv.saas.authentication.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 会员用户注册配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.authentication.app.member.register")
public class MemberUserRegisterConfig {

    /**
     * 随机登陆账户位数
     */
    private int randomUsernameCount = 6;

    /**
     * 你用户注册的默认分组
     */
    private int defaultGroup = 2;

    /**
     * 随机密码位数
     */
    private int randomPasswordCount = 16;
}
