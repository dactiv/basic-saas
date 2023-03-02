package com.github.dactiv.saas.workflow.service.approval.support;

import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import com.github.dactiv.saas.workflow.config.ScheduleConfig;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalResultEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyCopyStatusEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.github.dactiv.saas.workflow.service.ScheduleService;
import com.github.dactiv.saas.workflow.service.UserApplyHistoryService;
import com.github.dactiv.saas.workflow.service.approval.AbstractFormApprovalTypeResolver;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 或签流程审批解析器实现，只有有一个拒绝或同意，整个流程解释。
 *
 * @author maurice.chen
 */
@Component
public class UnanimousFormApprovalTypeResolver extends AbstractFormApprovalTypeResolver {

    public UnanimousFormApprovalTypeResolver(UserApplyHistoryService userApplyHistoryService,
                                             AmqpTemplate amqpTemplate,
                                             @Lazy ApplyService applyService,
                                             ScheduleConfig scheduleConfig,
                                             ScheduleService scheduleService) {
        super(userApplyHistoryService, amqpTemplate, applyService, scheduleConfig, scheduleService);
    }

    @Override
    public boolean support(FormApprovalTypeEnum type) {
        return FormApprovalTypeEnum.UNANIMOUS.equals(type);
    }

    @Override
    public List<ApplyApprovalEntity> createApplyApproval(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas) {
        return participantMetas.stream()
                .map(m -> ApplyApprovalEntity.of(m, body.getId()))
                .peek(a -> a.setStatus(ApplyApprovalStatusEnum.PROCESSING))
                .sorted(Comparator.comparing(ApplyApprovalEntity::getSort))
                .toList();
    }

    @Override
    public void approvalComplete(ApplyEntity apply, ApplyApprovalEntity entity) {

        super.approvalComplete(apply, entity);

        List<ApplyApprovalEntity> applyApprovalList = getApplyService()
                .getApplyApprovalService()
                .lambdaQuery()
                .select(ApplyApprovalEntity::getId, ApplyApprovalEntity::getApplyId, ApplyApprovalEntity::getUserId, VersionEntity::getVersion, ApplyApprovalEntity::getStatus)
                .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                .ne(ApplyApprovalEntity::getId, entity.getApplyId())
                .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                .list();

        if (ApplyApprovalResultEnum.AGREE.equals(entity.getResult())) {
            updateCreatorApplyStatus(apply, ApplyStatusEnum.AGREE);
        } else if (ApplyApprovalResultEnum.REFUSE.equals(entity.getResult())) {
            updateCreatorApplyStatus(apply, ApplyStatusEnum.REFUSE);
            getApplyService()
                    .getApplyCopyService()
                    .lambdaQuery()
                    .eq(ApplyCopyEntity::getApplyId, apply.getId())
                    .list()
                    .stream()
                    .peek(a -> a.setStatus(ApplyCopyStatusEnum.ABSTAIN))
                    .forEach(a -> getApplyService().getApplyCopyService().save(a));
        } else {
            throw new SystemException("ID 为 [" + entity.getId() + "] 的审批在审批之后不支持审批结果为 [" + entity.getResult().getName() + "] 的支持");
        }

        apply.setCompletionTime(new Date());
        getApplyService().save(apply);

        applyApprovalList
                .stream()
                .peek(a -> updateApproverStatusToProcessed(a.getApplyId(), a.getUserId()))
                .peek(a -> a.setStatus(ApplyApprovalStatusEnum.ABSTAIN))
                .peek(a -> a.setOperationTime(new Date()))
                .forEach(a -> getApplyService().getApplyApprovalService().save(a));

    }

}
