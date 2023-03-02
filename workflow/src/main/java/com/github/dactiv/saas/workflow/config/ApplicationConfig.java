package com.github.dactiv.saas.workflow.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("cmis.workflow.app")
public class ApplicationConfig {

    /**
     * 代办工作站内信标题
     */
    private String pendingWorkTitle = "您有一个新的{0}审批需要处理";

    /**
     * 代办工作站内信内容
     */
    private String pendingWorkContent = "{0}在{1}提出了{2}申请，请您审批，点击查看详情。";

    /**
     * 完成申请后的站内信标题
     */
    private String completeApplyTitle = "您提交的{0}申请{1}";

    /**
     * 完成申请后的站内信内容
     */
    private String completeApplyContent = "您提交的{0}申请在{1}已经审批完成，点击查看详情。";

    /**
     * 流程加急站内信标题
     */
    private String urgentTitle = "您有一个{0}的审批需加急";

    /**
     * 流程加急站内信标题
     */
    private String urgentContent = "{0}在{1}对{2}申请提出加急处理，请您尽快审批，点击查看详情。";

    private String aliYunAppCode;

    private CacheProperties holidayCache = CacheProperties.of(
            "cmis:workflow:schedule:holiday",
            TimeProperties.ofDay(1)
    );
}
