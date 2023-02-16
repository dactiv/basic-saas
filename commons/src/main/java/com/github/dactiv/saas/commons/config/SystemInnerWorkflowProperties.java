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

    @NonNull
    private String submitContent;

    @NonNull
    private String completeAuditMessageQueueName;

    @NonNull
    private String cancelAuditMessageQueueName;

    @NonNull
    private ApplyFormTypeEnum applyFormType;

    private Boolean enable = true;
}
