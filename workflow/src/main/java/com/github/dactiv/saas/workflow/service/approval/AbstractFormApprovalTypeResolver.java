package com.github.dactiv.saas.workflow.service.approval;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.domain.meta.workflow.FormMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.workflow.config.ScheduleConfig;
import com.github.dactiv.saas.workflow.consumer.SendCompleteApplyMessageConsumer;
import com.github.dactiv.saas.workflow.consumer.SendCompleteApplyNoticeConsumer;
import com.github.dactiv.saas.workflow.consumer.SendWorkMessageConsumer;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.entity.*;
import com.github.dactiv.saas.workflow.domain.meta.ScheduleFormMeta;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyCopyStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkTypeEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import com.github.dactiv.saas.workflow.service.ScheduleService;
import com.github.dactiv.saas.workflow.service.UserApplyHistoryService;
import com.github.dactiv.saas.workflow.service.schedule.FormDateResolver;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象的表单审批类型解析器实现
 *
 * @author maurice.chen
 */
public abstract class AbstractFormApprovalTypeResolver implements FormApprovalTypeResolver {

    private final UserApplyHistoryService userApplyHistoryService;

    @Getter
    private final AmqpTemplate amqpTemplate;

    @Getter
    private final ApplyService applyService;

    @Getter
    private final ScheduleService scheduleService;

    @Getter
    private final ScheduleConfig scheduleConfig;

    public AbstractFormApprovalTypeResolver(UserApplyHistoryService userApplyHistoryService,
                                            AmqpTemplate amqpTemplate,
                                            @Lazy ApplyService applyService,
                                            ScheduleConfig scheduleConfig,
                                            ScheduleService scheduleService) {
        this.userApplyHistoryService = userApplyHistoryService;
        this.amqpTemplate = amqpTemplate;
        this.applyService = applyService;
        this.scheduleConfig = scheduleConfig;
        this.scheduleService = scheduleService;
    }

    @Override
    public List<ApplyCopyEntity> createApplyCopy(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas) {
        if (CollectionUtils.isEmpty(participantMetas)) {
            return new LinkedList<>();
        }
        return participantMetas
                .stream()
                .map(m -> ApplyCopyEntity.of(m, body.getId()))
                .peek(a -> a.setStatus(ApplyCopyStatusEnum.WAITING))
                .sorted(Comparator.comparing(ApplyCopyEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void applyComplete(ApplyRequestBody body,
                              FormMeta form,
                              List<ApplyApprovalEntity> applyApprovalList,
                              List<ApplyCopyEntity> applyCopyList) {

        LambdaQueryChainWrapper<UserApplyHistoryEntity> wrapper = userApplyHistoryService
                .lambdaQuery()
                .select(UserApplyHistoryEntity::getId, UserApplyHistoryEntity::getParticipant)
                .eq(UserApplyHistoryEntity::getUserId, body.getUserId())
                .eq(UserApplyHistoryEntity::getFormType, body.getFormType().getValue());

        if (ApplyFormTypeEnum.SYSTEM_TYPE.contains(body.getFormType())) {
            wrapper = wrapper.eq(UserApplyHistoryEntity::getFormId, form.getId());
        } else {
            wrapper = wrapper.eq(UserApplyHistoryEntity::getFormId, body.getFormType().getValue());
        }

        UserApplyHistoryEntity userApplyHistoryEntity = wrapper.one();

        if (Objects.isNull(userApplyHistoryEntity)) {
            userApplyHistoryEntity = new UserApplyHistoryEntity();
            userApplyHistoryEntity.setUserDetails(body);

            if (ApplyFormTypeEnum.SYSTEM_TYPE.contains(body.getFormType())) {
                userApplyHistoryEntity.setFormId(form.getId());
                userApplyHistoryEntity.setFormName(form.getName());
            } else {
                userApplyHistoryEntity.setFormId(body.getFormType().getValue());
                userApplyHistoryEntity.setFormName(body.getFormType().getName());
            }

            userApplyHistoryEntity.setFormType(body.getFormType());

            userApplyHistoryEntity.setParticipant(body.getParticipantList());
        }

        userApplyHistoryEntity.setParticipant(body.getParticipantList());

        userApplyHistoryService.save(userApplyHistoryEntity);

    }

    @Override
    public void publishComplete(ApplyEntity apply) {

        List<ApplyApprovalEntity> approvalList = getApplyService()
                .getApplyApprovalService()
                .lambdaQuery()
                .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                .orderByAsc(ApplyApprovalEntity::getSort)
                .list();

        for (ApplyApprovalEntity approval : approvalList) {
            WorkEntity work = WorkEntity.of(apply, WorkTypeEnum.PENDING);

            work.setUsername(approval.getUsername());
            work.setUserId(approval.getUserId());
            work.setUserType(approval.getUserType());

            getApplyService().getWorkService().save(work);

        }

        sendPendingWorkMessage(apply.getId());
    }

    @Override
    public void applyAllComplete(ApplyEntity apply, FormMeta form) throws Exception {

        sendCompleteApplyMessage(apply.getId());

        if (!ApplyFormTypeEnum.SYSTEM_TYPE.contains(apply.getFormType())) {
            sendCustomApplyNotice(apply);
        }

        if (ApplyStatusEnum.REFUSE.equals(apply.getStatus())) {
            return ;
        }

        addSchedule(apply, form);
    }

    private void sendCustomApplyNotice(ApplyEntity apply) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                getAmqpTemplate().convertAndSend(
                        SystemConstants.SYS_WORKFLOW_RABBITMQ_EXCHANGE,
                        SendCompleteApplyNoticeConsumer.DEFAULT_QUEUE_NAME,
                        apply.getId(),
                        message -> {
                            String id = SendCompleteApplyNoticeConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + apply.getId();
                            message.getMessageProperties().setMessageId(id);
                            message.getMessageProperties().setCorrelationId(apply.getId().toString());
                            return message;
                        }
                );
            }
        });
    }

    public void addSchedule(ApplyEntity apply, FormMeta form) throws Exception {
        Optional<ScheduleConfig.ScheduleForm> optional = scheduleConfig
                .getAutoInsertByForm()
                .stream()
                .filter(a -> a.getId().equals(form.getId()))
                .findFirst();

        if (optional.isEmpty()) {
            return ;
        }

        ScheduleConfig.ScheduleForm scheduleForm = optional.get();

        Class<? extends FormDateResolver> resolver = scheduleForm.getType().getFormDateResolverClass();

        FormDateResolver formDateResolver = resolver.getConstructor().newInstance();

        Date startTime = formDateResolver.getStartDate(apply.getApplyContent());
        if (Objects.isNull(startTime)) {
            return;
        }

        Date endTime = formDateResolver.getEndDate(apply.getApplyContent());
        ScheduleEntity schedule = new ScheduleEntity();

        schedule.setName(form.getName());

        if (ScheduleFormMeta.class.isAssignableFrom(form.getClass())) {
            ScheduleFormMeta scheduleFormMeta = Casts.cast(form);
            schedule.setName(StringUtils.defaultIfEmpty(scheduleFormMeta.getScheduleName(), scheduleFormMeta.getName()));
        }

        schedule.setUserId(apply.getUserId());
        schedule.setUsername(apply.getUsername());
        schedule.setUserType(apply.getUserType());

        schedule.setStartTime(startTime);
        schedule.setEndTime(Objects.requireNonNullElse(endTime, startTime));

        Object content = apply.getApplyContent().get(scheduleForm.getContentKey());
        if (Objects.nonNull(content)) {
            schedule.setContent(content.toString());
        } else {
            schedule.setContent(apply.getFormName());
        }

        scheduleService.save(schedule);
    }

    @Override
    public void approvalComplete(ApplyEntity apply, ApplyApprovalEntity applyApproval) {

        WorkEntity work = getProcessingStatusWork(applyApproval.getUserId(), apply.getId());
        updateWorkStatusToProcessed(work);

    }

    protected WorkEntity getProcessingStatusWork(Integer userId, Integer applyId) {
        return getApplyService()
                .getWorkService()
                .lambdaQuery()
                .select(WorkEntity::getId, WorkEntity::getStatus, WorkEntity::getType, VersionEntity::getVersion)
                .eq(WorkEntity::getApplyId, applyId)
                .eq(WorkEntity::getUserId, userId)
                .eq(WorkEntity::getType, WorkTypeEnum.PENDING.getValue())
                .eq(WorkEntity::getStatus, WorkStatusEnum.PROCESSING.getValue())
                .one();
    }

    protected void updateWorkStatusToProcessed(WorkEntity work) {
        if (Objects.nonNull(work)) {
            work.setStatus(WorkStatusEnum.PROCESSED);
            work.setType(WorkTypeEnum.PROCESSED);
            applyService.getWorkService().save(work);
        }
    }

    protected void updateCreatorApplyStatus(ApplyEntity apply, ApplyStatusEnum status) {
        apply.setStatus(status);
        apply.setCompletionTime(new Date());
        getApplyService().save(apply);

        WorkEntity work = applyService
                .getWorkService()
                .lambdaQuery()
                .eq(WorkEntity::getApplyId, apply.getId())
                .eq(WorkEntity::getUserId, apply.getUserId())
                .eq(WorkEntity::getType, WorkTypeEnum.CREATED.getValue())
                .one();

        work.setStatus(WorkStatusEnum.PROCESSED);

        applyService.getWorkService().save(work);
    }

    protected void updateApproverStatusToProcessed(Integer applyId, Integer userId) {
        WorkEntity work = Objects.requireNonNull(getProcessingStatusWork(userId, applyId),"找不到 applyId 为 [" + applyId + "] userId 为 [" + userId + "] 的工作表记录");
        updateWorkStatusToProcessed(work);
    }

    protected void sendCompleteApplyMessage(Integer applyId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                getAmqpTemplate().convertAndSend(
                        SystemConstants.SYS_WORKFLOW_RABBITMQ_EXCHANGE,
                        SendCompleteApplyMessageConsumer.DEFAULT_QUEUE_NAME,
                        applyId,
                        message -> {
                            String id = SendCompleteApplyMessageConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + applyId;
                            message.getMessageProperties().setMessageId(id);
                            message.getMessageProperties().setCorrelationId(applyId.toString());
                            return message;
                        }
                );
            }
        });
    }

    protected void sendPendingWorkMessage(Integer applyId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                getAmqpTemplate().convertAndSend(
                        SystemConstants.SYS_WORKFLOW_RABBITMQ_EXCHANGE,
                        SendWorkMessageConsumer.DEFAULT_QUEUE_NAME,
                        applyId,
                        message -> {
                            String id = SendWorkMessageConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + applyId;
                            message.getMessageProperties().setMessageId(id);
                            message.getMessageProperties().setCorrelationId(applyId.toString());
                            return message;
                        }
                );
            }
        });
    }
}
