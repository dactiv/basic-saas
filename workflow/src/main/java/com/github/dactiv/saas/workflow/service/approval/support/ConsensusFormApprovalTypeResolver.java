package com.github.dactiv.saas.workflow.service.approval.support;

import com.github.dactiv.framework.commons.exception.SystemException;
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
 * 会签流程审批解析器实现，需要全部通过才能通过。
 *
 * @author maurice.chen
 */
@Component
public class ConsensusFormApprovalTypeResolver extends AbstractFormApprovalTypeResolver {

    public ConsensusFormApprovalTypeResolver(UserApplyHistoryService userApplyHistoryService,
                                             AmqpTemplate amqpTemplate,
                                             @Lazy ApplyService applyService,
                                             ScheduleConfig scheduleConfig,
                                             ScheduleService scheduleService) {
        super(userApplyHistoryService, amqpTemplate, applyService, scheduleConfig, scheduleService);
    }

    @Override
    public boolean support(FormApprovalTypeEnum type) {
        return FormApprovalTypeEnum.CONSENSUS.equals(type);
    }

    @Override
    public List<ApplyApprovalEntity> createApplyApproval(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas) {
        return participantMetas
                .stream()
                .map(m -> ApplyApprovalEntity.of(m, body.getId()))
                .peek(a -> a.setStatus(ApplyApprovalStatusEnum.PROCESSING))
                .sorted(Comparator.comparing(ApplyApprovalEntity::getSort))
                .toList();
    }

    @Override
    public void approvalComplete(ApplyEntity apply, ApplyApprovalEntity entity) {

        super.approvalComplete(apply, entity);

        if (ApplyApprovalResultEnum.AGREE.equals(entity.getResult())) {
            boolean exist = getApplyService()
                    .getApplyApprovalService()
                    .lambdaQuery()
                    .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                    .ne(ApplyApprovalEntity::getId, entity.getApplyId())
                    .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                    .exists();

            if (!exist) {
                updateCreatorApplyStatus(apply, ApplyStatusEnum.AGREE);
            }
        } else if (ApplyApprovalResultEnum.REFUSE.equals(entity.getResult())) {
            List<ApplyApprovalEntity> applyApprovalList = getApplyService()
                    .getApplyApprovalService()
                    .lambdaQuery()
                    .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                    .ne(ApplyApprovalEntity::getId, entity.getApplyId())
                    .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                    .list();

            applyApprovalList
                    .stream()
                    .peek(a -> updateApproverStatusToProcessed(a.getApplyId(), a.getUserId()))
                    .peek(a -> a.setStatus(ApplyApprovalStatusEnum.ABSTAIN))
                    .peek(a -> a.setOperationTime(new Date()))
                    .forEach(a -> getApplyService().getApplyApprovalService().save(a));
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
    }

}
