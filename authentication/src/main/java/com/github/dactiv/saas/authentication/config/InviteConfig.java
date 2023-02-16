package com.github.dactiv.saas.authentication.config;


import com.github.dactiv.saas.commons.SystemConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * 邀请配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.authentication.app.invite")
public class InviteConfig {

    /**
     * 邀请教师的站内信标题
     */
    private String teacherTitle = "您收到一个来自 {0} 邀请";

    /**
     * 邀请教师的站内信内容
     */
    private String teacherContent = "尊敬的教师您好，[{0}]邀请您成为该学校的教师，请你接受获/拒绝本次邀请处理。";

    /**
     * 教师接受邀请反馈标题
     */
    private String teacherFeedbackTitle = "您发起的教师邀请已反馈";

    /**
     * 教师接受邀请反馈内容
     */
    private String teacherFeedbackContent = "尊敬的用户您好，您邀请的[{0}]老师已处理邀请，该邀请处理结果为{1} 。";

    /**
     * 邀请 api 接口名称
     */
    private String apiName = StringUtils.appendIfMissing(SystemConstants.SYS_AUTHENTICATION_NAME, AntPathMatcher.DEFAULT_PATH_SEPARATOR) + "invite";

}
