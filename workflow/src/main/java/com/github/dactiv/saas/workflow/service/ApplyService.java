package com.github.dactiv.saas.workflow.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.WorkflowConstants;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.domain.dto.workflow.UserAuditOperationDto;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.domain.meta.workflow.FormMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.AuditOperationTypeEnum;
import com.github.dactiv.saas.commons.enumeration.FormParticipantTypeEnum;
import com.github.dactiv.saas.workflow.consumer.SendCompleteApplyNoticeConsumer;
import com.github.dactiv.saas.workflow.consumer.SendUrgentMessageConsumer;
import com.github.dactiv.saas.workflow.dao.ApplyDao;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.body.response.ApplyResponseBody;
import com.github.dactiv.saas.workflow.domain.body.response.WorkResponseBody;
import com.github.dactiv.saas.workflow.domain.entity.*;
import com.github.dactiv.saas.workflow.enumerate.*;
import com.github.dactiv.saas.workflow.service.approval.FormApprovalTypeResolver;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_apply ???????????????
 *
 * <p>Table: tb_apply - ???????????????</p>
 *
 * @author maurice.chen
 * @see ApplyEntity
 * @since 2022-03-03 02:31:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplyService extends BasicService<ApplyDao, ApplyEntity> {

    @Getter
    private final ApplyApprovalService applyApprovalService;

    @Getter
    private final ApplyCopyService applyCopyService;

    private final AmqpTemplate amqpTemplate;

    private final ConcurrentInterceptor concurrentInterceptor;

    @Getter
    private final WorkService workService;

    private final FormService formService;

    private final List<FormApprovalTypeResolver> formApprovalTypeResolvers;

    public ApplyService(ApplyApprovalService applyApprovalService,
                        ApplyCopyService applyCopyService,
                        AmqpTemplate amqpTemplate,
                        ConcurrentInterceptor concurrentInterceptor,
                        WorkService workService,
                        @Lazy FormService formService,
                        ObjectProvider<FormApprovalTypeResolver> formApprovalTypeResolvers) {

        this.applyCopyService = applyCopyService;
        this.applyApprovalService = applyApprovalService;
        this.amqpTemplate = amqpTemplate;
        this.concurrentInterceptor = concurrentInterceptor;
        this.workService = workService;
        this.formService = formService;
        this.formApprovalTypeResolvers = formApprovalTypeResolvers.orderedStream().toList();
    }

    /**
     * ?????????????????????
     *
     * @param entity ??????????????????
     * @param isPublish ???????????????0.???,1.???
     *
     * @return ????????????
     */
    public int save(ApplyEntity entity, boolean isPublish) {
        int result = save(entity);

        if (isPublish) {
            publish(entity);
        }

        return result;
    }

    @Override
    public int save(ApplyEntity entity) {

        boolean isNew = Objects.isNull(entity.getId());

        if (isNew) {
            entity.setStatus(ApplyStatusEnum.NEW);
        }

        FormMeta formMeta;
        if (ApplyFormTypeEnum.CUSTOM.equals(entity.getFormType())) {
            FormEntity form = formService.get(entity.getFormId());
            Assert.notNull(form, "????????? ID ??? [" + entity.getFormId() + "] ?????????????????????");

            entity.setFormContent(form.getDesign());
            entity.setApprovalType(form.getApprovalType());

            formMeta = form;
        } else {
            formMeta = new FormMeta();
            formMeta.setId(entity.getFormId());
            formMeta.setName(entity.getFormName());
        }

        int result = super.save(entity);

        if (ApplyRequestBody.class.isAssignableFrom(entity.getClass())) {

            ApplyRequestBody body = Casts.of(entity, ApplyRequestBody.class);

            FormApprovalTypeResolver resolver = formApprovalTypeResolvers
                    .stream()
                    .filter(f -> f.support(entity.getApprovalType()))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("???????????????????????? [" + entity.getFormName() + "] ????????????"));

            Map<FormParticipantTypeEnum, List<AuditParticipantMeta>> participantGroup = body
                    .getParticipantList()
                    .stream()
                    .sorted(Comparator.comparing(AuditParticipantMeta::getSort))
                    .collect(Collectors.groupingBy(AuditParticipantMeta::getType));

            List<ApplyApprovalEntity> applyApprovalList = resolver.createApplyApproval(body, participantGroup.get(FormParticipantTypeEnum.APPROVER));
            List<ApplyCopyEntity> applyCopyList = resolver.createApplyCopy(body, participantGroup.get(FormParticipantTypeEnum.COPY));

            body.setApprovalCount(applyApprovalList.size());

            applyApprovalList.forEach(a -> a.setApplyId(body.getId()));
            applyCopyList.forEach(a -> a.setApplyId(body.getId()));

            applyApprovalService.lambdaUpdate().eq(ApplyApprovalEntity::getApplyId, body.getId()).remove();
            applyApprovalService.save(applyApprovalList);

            applyCopyService.lambdaUpdate().eq(ApplyCopyEntity::getApplyId, body.getId()).remove();
            applyCopyService.save(applyCopyList);

            resolver.applyComplete(body, formMeta, applyApprovalList, applyCopyList);
        }

        return result;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "?????? id ??? [" + ids + "] ??? [" + getEntityClass() + "] ???????????????";
            throw new SystemException(msg);
        }
        return result;
    }

    /**
     * ??????????????????
     *
     * @param id ?????? id
     */
    private void deleteAssociatedData(Serializable id) {
        applyApprovalService.lambdaUpdate().eq(ApplyApprovalEntity::getApplyId, id).remove();
        applyCopyService.lambdaUpdate().eq(ApplyCopyEntity::getApplyId, id).remove();
        workService.lambdaUpdate().eq(WorkEntity::getApplyId, id).remove();
    }

    @Override
    public int deleteById(Serializable id) {
        ApplyEntity apply = get(id);
        return deleteByEntity(apply);
    }

    @Override
    public int deleteByEntity(Collection<ApplyEntity> entities, boolean errorThrow) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "????????????????????? [" + entities + "] ??? [" + getEntityClass() + "] ???????????????";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteByEntity(ApplyEntity entity) {
        if(!ApplyStatusEnum.SUBMIT_STATUS.contains(entity.getStatus())) {
            throw new ServiceException(entity.getFormName() + "??????????????????????????????");
        }

        int result = super.deleteByEntity(entity);

        deleteAssociatedData(entity.getId());

        return result;
    }

    @Override
    public int delete(Wrapper<ApplyEntity> wrapper) {
        throw new UnsupportedOperationException("??????????????????");
    }

    /**
     * ???????????????????????????
     *
     * @param entity ??????????????????
     * @return ?????????????????????????????????
     */
    public ApplyResponseBody convertApplyBody(ApplyEntity entity) {

        ApplyResponseBody body = Casts.of(entity, ApplyResponseBody.class);

        List<ApplyApprovalEntity> approvalList = applyApprovalService
                .lambdaQuery()
                .eq(ApplyApprovalEntity::getApplyId, entity.getId())
                .orderByAsc(ApplyApprovalEntity::getSort)
                .list();

        List<ApplyCopyEntity> copyList = applyCopyService
                .lambdaQuery()
                .eq(ApplyCopyEntity::getApplyId, entity.getId())
                .orderByAsc(ApplyCopyEntity::getSort)
                .list();

        body.setApprovalList(approvalList);
        body.setCopyList(copyList);

        return body;
    }

    /**
     * ??????????????????
     *
     * @param ids ?????? id ??????
     * @param userId ?????? id
     */
    public void publish(List<Integer> ids, Integer userId) {
        ids.forEach(id -> this.publish(id, userId));
    }

    /**
     * ??????????????????
     *
     * @param id     ?????? id
     * @param userId ???????????? id
     */
    public void publish(Integer id, Integer userId) {

        ApplyEntity entity = Objects.requireNonNull(get(id), "????????? ID ??? [" + id + "] ?????????????????????");

        Assert.isTrue(Objects.equals(entity.getUserId(), userId), "ID ??? [" + id + "] ?????????????????????, ????????? ID ??? [" + userId + "] ?????????");

        publish(entity);
    }

    public void publish(ApplyEntity entity) {
        if (ApplyStatusEnum.EXECUTING.equals(entity.getStatus())) {
            throw new ServiceException("????????????????????????");
        }

        entity.setStatus(ApplyStatusEnum.EXECUTING);
        save(entity);

        WorkEntity work = WorkEntity.of(entity, WorkTypeEnum.CREATED);

        workService.save(work);

        FormApprovalTypeResolver resolver = formApprovalTypeResolvers
                .stream()
                .filter(f -> f.support(entity.getApprovalType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("???????????????????????? [" + entity.getApprovalType() + "] ????????????"));

        resolver.publishComplete(entity);
    }

    /**
     * ????????????
     *
     * @param id     ?????? id
     * @param userId ????????????
     * @return ????????????
     */
    @Concurrent(value = "dactiv:saas:workflow:apply-update:[#id]", condition = "[#id] != null")
    public RestResult<Integer> approval(Integer id, Integer userId, Integer result, String remark) throws Exception {
        Assert.notNull(id, "?????? id ????????????");

        ApplyApprovalEntity entity = applyApprovalService
                .lambdaQuery()
                .eq(ApplyApprovalEntity::getApplyId, id)
                .eq(ApplyApprovalEntity::getUserId, userId)
                .one();

        Assert.notNull(entity, "????????? ID ??? [" + id + "] ?????????????????????");

        if (!ApplyApprovalStatusEnum.PROCESSING.equals(entity.getStatus())) {
            throw new ServiceException("???????????????????????????????????????????????????");
        }

        entity.setStatus(ApplyApprovalStatusEnum.COMPLETE);
        entity.setResult(ValueEnumUtils.parse(result, ApplyApprovalResultEnum.class));
        entity.setRemark(StringUtils.defaultString(remark, entity.getResult().getName()));
        entity.setOperationTime(new Date());

        applyApprovalService.save(entity);

        ApplyEntity apply = get(entity.getApplyId());
        Assert.notNull(apply, "????????? ID ??? [" + entity.getApplyId() + "] ?????????????????????");

        FormApprovalTypeResolver resolver = formApprovalTypeResolvers
                .stream()
                .filter(f -> f.support(apply.getApprovalType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("???????????????????????? [" + apply.getApprovalType() + "] ????????????"));

        resolver.approvalComplete(apply, entity);

        if (ApplyStatusEnum.SCHEDULE_STATUS.contains(apply.getStatus())) {

            FormMeta formMeta;

            if (ApplyFormTypeEnum.CUSTOM.equals(apply.getFormType())) {
                FormEntity form = formService.get(apply.getFormId());
                Assert.notNull(form, "????????? ID ??? [" + apply.getFormId() + "] ?????????????????????");
                formMeta = form;
            } else {
                formMeta = new FormMeta();
                formMeta.setId(apply.getFormId());
                formMeta.setName(apply.getFormName());
            }

            resolver.applyAllComplete(apply, formMeta);
        }

        return RestResult.ofSuccess(entity.getStatus().getName(), entity.getId());
    }

    /**
     * ????????????
     *
     * @param id ?????????????????? id
     * @param userId ???????????? id
     */
    public void urgent(Integer id, Integer userId) {
        ApplyEntity apply = get(id);

        if (ApplyStatusEnum.SCHEDULE_STATUS.contains(apply.getStatus())) {
            throw new ServiceException("????????????????????????");
        }

        if (!apply.getUserId().equals(userId)) {
            throw new ServiceException("?????????????????????????????????");
        }

        apply.setUrgentCount(apply.getUrgentCount() + 1);
        apply.setUrgingTime(new Date());

        save(apply);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.SYS_WORKFLOW_RABBITMQ_EXCHANGE,
                        SendUrgentMessageConsumer.DEFAULT_QUEUE_NAME,
                        apply.getId(),
                        message -> {
                            String id = SendUrgentMessageConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + apply.getId();
                            message.getMessageProperties().setMessageId(id);
                            message.getMessageProperties().setCorrelationId(apply.getId().toString());
                            return message;
                        }
                );
            }
        });
    }

    /**
     * ???????????????????????????
     *
     * @param entity ??????????????????
     * @param loadApproval ????????????????????????true ???????????? false
     * @return ?????????????????????????????????
     */
    public WorkResponseBody convertWorkResponseBody(WorkEntity entity, boolean loadApproval) {

        WorkResponseBody body = Casts.of(entity, WorkResponseBody.class);

        if (Objects.nonNull(entity.getApplyId())) {
            ApplyEntity apply = get(entity.getApplyId());
            ApplyResponseBody applyResponseBody = Casts.of(apply, ApplyResponseBody.class);

            if (loadApproval) {

                List<ApplyApprovalEntity> approvalList = applyApprovalService
                        .lambdaQuery()
                        .eq(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.PROCESSING.getValue())
                        .eq(ApplyApprovalEntity::getApplyId, apply.getId())
                        .list();

                applyResponseBody.setApprovalList(approvalList);
            }

            body.setApply(applyResponseBody);
        }

        return body;
    }

    /**
     * ??????????????????
     *
     * @param id ?????? id
     * @param fieldName ????????????
     * @param filename ????????????
     */
    public void deleteFileInfo(Integer id, String fieldName, String filename) {
        ApplyEntity entity = Objects.requireNonNull(get(id), "????????? ID ??? [" + id + "] ?????????????????????");

        Assert.isTrue(ApplyStatusEnum.SUBMIT_STATUS.contains(entity.getStatus()), "ID ??? [" + id + "] ??? [" + entity.getFormName() + "] ????????????????????????????????????");

        Map<String, Object> map = entity.getApplyContent();

        Object files = map.get(fieldName);
        if (Objects.isNull(files)) {
            return ;
        }

        List<Map<String, Object>> fileList = Casts.cast(files);

        for (Map<String, Object> fileMap : fileList) {
            Object name = fileMap.get(SystemConstants.MINIO_OBJECT_NAME);
            if (Objects.isNull(name)) {
                continue;
            }
            if (StringUtils.equals(name.toString(), filename)) {
                fileList.remove(fileMap);
                break;
            }
        }

        map.put(fieldName, fileList);
        updateById(entity);
    }

    /**
     * ????????????
     *
     * @param ids ?????? id ??????
     * @param user ?????? id
     */
    public void cancel(List<Integer> ids, BasicUserDetails<Integer> user) {
        ids.stream().distinct().forEach(id -> concurrentInterceptor.invoke("dactiv:saas:workflow:apply-update:" + id, () -> cancel(id, user, true, true)));
    }

    /**
     * ????????????
     *
     * @param id ?????? id
     * @param user ??????????????????
     */
    @Concurrent(value = "dactiv:saas:workflow:apply-update:[#id]", condition = "[#id] != null")
    public RestResult<?> cancel(Integer id, TypeUserDetails<Integer> user, boolean throwError, boolean checkUser) {
        ApplyEntity entity = Objects.requireNonNull(get(id), "????????? ID ??? [" + id + "] ?????????????????????");

        if (!ApplyStatusEnum.EXECUTING.equals(entity.getStatus())) {
            ServiceException exception = new ServiceException("ID ??? [" + entity.getId() + "] ??? [" + entity.getFormName() + "] ????????????????????????????????????");
            if (throwError) {
                throw exception;
            } else {
                return RestResult.ofException(String.valueOf(HttpStatus.NOT_MODIFIED.value()), exception);
            }
        }

        if (checkUser) {
            Assert.isTrue(Objects.equals(entity.getUserId(), user.getUserId()) && Objects.equals(entity.getUserType(), user.getUserType()), "ID ??? [" + id + "] ?????????????????????, ????????? ID ??? [" + user.getUsername() + "] ??????");
        }
        entity.setStatus(ApplyStatusEnum.CANCEL);
        entity.setCancellationTime(new Date());

        List<ApplyApprovalEntity> approvalList = applyApprovalService
                .lambdaQuery()
                .in(ApplyApprovalEntity::getStatus, ApplyApprovalStatusEnum.CANCEL_APPLY_STATUS)
                .eq(ApplyApprovalEntity::getApplyId, entity.getId())
                .list();

        approvalList
                .stream()
                .peek(a -> a.setStatus(ApplyApprovalStatusEnum.ABSTAIN))
                .peek(a -> a.setOperationTime(new Date()))
                .forEach(applyApprovalService::updateById);

        List<ApplyCopyEntity> copyList = applyCopyService
                .lambdaQuery()
                .eq(ApplyCopyEntity::getApplyId, entity.getId())
                .list();

        copyList
                .stream()
                .peek(c -> c.setStatus(ApplyCopyStatusEnum.ABSTAIN))
                .forEach(applyCopyService::updateById);

        workService
                .lambdaQuery()
                .eq(WorkEntity::getApplyId, entity.getId())
                .eq(WorkEntity::getStatus, WorkStatusEnum.PROCESSING.getValue())
                .list()
                .stream()
                .peek(w -> w.setStatus(WorkStatusEnum.CANCEL))
                .forEach(workService::updateById);

        updateById(entity);

        if (entity.getApplyContent().containsKey(WorkflowConstants.CANCEL_AUDIT_NOTICE_MESSAGE_QUEUE_NAME)) {

            Object queueName = entity.getApplyContent().get(WorkflowConstants.CANCEL_AUDIT_NOTICE_MESSAGE_QUEUE_NAME);
            Object exchangeName = entity.getApplyContent().get(WorkflowConstants.CANCEL_AUDIT_NOTICE_MESSAGE_EXCHANGE_NAME);

            UserAuditOperationDto dto = new UserAuditOperationDto();

            dto.setUserDetails(user);
            dto.setOperationType(AuditOperationTypeEnum.CANCEL);
            dto.setFormType(entity.getFormType());
            dto.setApplyId(entity.getId());
            dto.setId(entity.getFormId());

            SendCompleteApplyNoticeConsumer.sendNoticeConsumer(exchangeName, queueName, dto, amqpTemplate);

        }

        return RestResult.of("????????????");
    }

    /**
     * ???????????????????????????
     *
     * @param dto ??????????????? dto
     */
    public Integer createCustomApply(CreateCustomApplyDto dto) {
        boolean exist = lambdaQuery()
                .eq(ApplyEntity::getFormId, dto.getId())
                .eq(ApplyEntity::getFormType, dto.getApprovalType().getValue())
                .exists();

        if (exist) {
            throw new ServiceException("?????? ID ??? [" + dto.getId() + "] ????????????????????????");
        }

        ApplyRequestBody apply = ApplyRequestBody.of(dto);

        save(apply, true);

        return apply.getId();
    }
}
