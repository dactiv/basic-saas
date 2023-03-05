package com.github.dactiv.saas.commons.config;

import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 系统内部工作流配置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class SystemInnerWorkflowProperties {

    /**
     * 提交内容
     */
    @NonNull
    private String submitContent;

    /**
     * 完成审核后要发送到的消息队列
     */
    @NonNull
    private String completeAuditMessageQueueName;

    /**
     * 取消审核后要发送到的消息队列
     */
    @NonNull
    private String cancelAuditMessageQueueName;

    /**
     * 自定义申请类型
     */
    @NonNull
    private ApplyFormTypeEnum applyFormType;

    /**
     * 是否启用
     */
    private Boolean enable = true;
}
