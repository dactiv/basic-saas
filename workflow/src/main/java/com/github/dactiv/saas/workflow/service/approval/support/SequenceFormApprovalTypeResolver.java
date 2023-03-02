package com.github.dactiv.saas.workflow.service.approval.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import com.github.dactiv.saas.workflow.config.ScheduleConfig;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalResultEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyCopyStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkTypeEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.github.dactiv.saas.workflow.service.ScheduleService;
import com.github.dactiv.saas.workflow.service.UserApplyHistoryService;
import com.github.dactiv.saas.workflow.service.approval.AbstractFormApprovalTypeResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 顺序流程审批解析器实现，先一个通过，在到下一个审批。如果中间有一个不通过，全部不通过。
 *
 * @author maurice.chen
 */
@Component
public class SequenceFormApprovalTypeResolver extends AbstractFormApprovalTypeResolver {

    public SequenceFormApprovalTypeResolver(UserApplyHistoryService userApplyHistoryService,
                                            AmqpTemplate amqpTemplate,
                                            @Lazy ApplyService applyService,
                                            ScheduleConfig scheduleConfig,
                                            ScheduleService scheduleService) {
        super(userApplyHistoryService, amqpTemplate, applyService, scheduleConfig, scheduleService);
    }

    @Override
    public boolean support(FormApprovalTypeEnum type) {
        return FormApprovalTypeEnum.SEQUENCE.equals(type);
    }

    @Override
    public List<ApplyApprovalEntity> createApplyApproval(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas) {

        List<ApplyApprovalEntity> result = participantMetas
                .stream()
                .map(m -> ApplyApprovalEntity.of(m, body.getId()))
                .peek(a -> a.setStatus(ApplyApprovalStatusEnum.WAITING))
                .sorted(Comparator.comparing(ApplyApprovalEntity::getSort))
                .toList();

        result.iterator().next().setStatus(ApplyApprovalStatusEnum.PROCESSING);

        return result;
    }

    @Override
    public void approvalComplete(ApplyEntity apply, ApplyApprovalEntity entity) {

        super.approvalComplete(apply, entity);

        if (ApplyApprovalResultEnum.AGREE.equals(entity.getResult())) {
            Wrapper<ApplyApprovalEntity> wrapper = Wrappers
                    .<ApplyApprovalEntity>lambdaQuery()
                    .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                    .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.WAITING.getValue())
                    .ne(ApplyApprovalEntity::getId, entity.getApplyId())
                    .gt(ApplyApprovalEntity::getSort, entity.getSort())
                    .orderByAsc(ApplyApprovalEntity::getSort);

            List<ApplyApprovalEntity> approvalEntityList = getApplyService()
                    .getApplyApprovalService()
                    .findPage(PageRequest.of(PageRequest.DEFAULT_PAGE), wrapper)
                    .getElements();

            if (CollectionUtils.isEmpty(approvalEntityList)) {
                updateCreatorApplyStatus(apply, ApplyStatusEnum.AGREE);
            } else {
                ApplyApprovalEntity next = approvalEntityList.iterator().next();
                next.setStatus(ApplyApprovalStatusEnum.PROCESSING);
                addNewWork(apply, next);
                getApplyService().getApplyApprovalService().save(next);

                sendPendingWorkMessage(apply.getId());
            }

        } else if (ApplyApprovalResultEnum.REFUSE.equals(entity.getResult())) {

            List<ApplyApprovalEntity> nextList = getApplyService()
                    .getApplyApprovalService()
                    .lambdaQuery()
                    .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                    .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.WAITING.getValue())
                    .ne(ApplyApprovalEntity::getId, entity.getApplyId())
                    .list();

            nextList
                    .stream()
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

    @Override
    public void publishComplete(ApplyEntity apply) {
        Wrapper<ApplyApprovalEntity> wrapper = Wrappers
                .<ApplyApprovalEntity>lambdaQuery()
                .select(ApplyApprovalEntity::getUserId, ApplyApprovalEntity::getUsername, ApplyApprovalEntity::getUserType)
                .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                .orderByAsc(ApplyApprovalEntity::getSort);

        Page<ApplyApprovalEntity> page = getApplyService()
                .getApplyApprovalService()
                .findPage(PageRequest.of(PageRequest.DEFAULT_PAGE), wrapper);

        ApplyApprovalEntity applyApproval = page.getElements().iterator().next();

        addNewWork(apply, applyApproval);
    }

    public void addNewWork(ApplyEntity apply, ApplyApprovalEntity applyApproval) {

        WorkEntity work = WorkEntity.of(apply, WorkTypeEnum.PENDING);

        work.setUsername(applyApproval.getUsername());
        work.setUserId(applyApproval.getUserId());
        work.setUserType(applyApproval.getUserType());

        getApplyService().getWorkService().save(work);

        sendPendingWorkMessage(apply.getId());
    }
}
