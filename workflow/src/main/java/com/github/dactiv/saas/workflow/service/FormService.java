package com.github.dactiv.saas.workflow.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.FormParticipantTypeEnum;
import com.github.dactiv.saas.workflow.dao.FormDao;
import com.github.dactiv.saas.workflow.domain.body.FormBody;
import com.github.dactiv.saas.workflow.domain.entity.*;
import com.github.dactiv.saas.workflow.enumerate.FormStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_form 的业务逻辑
 *
 * <p>Table: tb_form - 流程表单表</p>
 *
 * @author maurice.chen
 * @see FormEntity
 * @since 2022-03-03 02:31:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FormService extends BasicService<FormDao, FormEntity> {

    private final FormParticipantService formParticipantService;

    private final ApplyService applyService;

    private final ConcurrentInterceptor concurrentInterceptor;

    public FormService(FormParticipantService formParticipantService,
                       @Lazy ApplyService applyService,
                       ConcurrentInterceptor concurrentInterceptor) {
        this.formParticipantService = formParticipantService;
        this.applyService = applyService;
        this.concurrentInterceptor = concurrentInterceptor;
    }

    @Override
    @Concurrent(value = "cmis:workflow:from:save:[#entity.id]", condition = "[#entity.id] != null")
    public int save(FormEntity entity) {
        boolean isNew = Objects.isNull(entity.getId());

        if (isNew) {
            entity.setStatus(FormStatusEnum.NEW);
        }

        int result = super.save(entity);

        if (FormBody.class.isAssignableFrom(entity.getClass())) {

            FormBody body = Casts.cast(entity);

            if (!isNew) {
                formParticipantService.lambdaUpdate().eq(FormParticipantEntity::getFormId, entity.getId()).remove();
            }

            if (CollectionUtils.isNotEmpty(body.getParticipantList())) {

                List<FormParticipantEntity> formParticipantList = body
                        .getParticipantList()
                        .stream()
                        .map(m -> FormParticipantEntity.of(m, body.getId()))
                        .collect(Collectors.toList());

                result += formParticipantList.size();
                formParticipantService.save(formParticipantList);
            }

        }

        return result;
    }

    /**
     * 转换流程表单请求响应体
     *
     * @param entity 备课管理实体
     * @return 备课管理的请求和响应体
     */
    public FormBody convertFormBody(FormEntity entity) {

        FormBody body = Casts.of(entity, FormBody.class);

        if (YesOrNo.Yes.equals(body.getParticipant())) {

            List<FormParticipantEntity> participantList = formParticipantService
                    .lambdaQuery()
                    .eq(FormParticipantEntity::getFormId, entity.getId())
                    .list();
            body.setParticipantList(participantList.stream().map(FormService::of).collect(Collectors.toList()));
        }

        return body;
    }

    public static AuditParticipantMeta of(ApplyApprovalEntity entity) {
        AuditParticipantMeta meta = new AuditParticipantMeta();

        meta.setSort(entity.getSort());
        meta.setUserId(entity.getUserId());
        meta.setUsername(entity.getUsername());
        meta.setUserType(entity.getUserType());
        meta.setType(FormParticipantTypeEnum.APPROVER);

        return meta;
    }

    public static AuditParticipantMeta of(ApplyCopyEntity entity) {
        AuditParticipantMeta meta = new AuditParticipantMeta();

        meta.setSort(entity.getSort());
        meta.setType(FormParticipantTypeEnum.COPY);

        meta.setUserId(entity.getUserId());
        meta.setUsername(entity.getUsername());
        meta.setUserType(entity.getUserType());

        return meta;
    }

    public static AuditParticipantMeta of(FormParticipantEntity entity) {
        AuditParticipantMeta meta = new AuditParticipantMeta();

        meta.setSort(entity.getSort());
        meta.setType(entity.getType());

        meta.setUserId(entity.getUserId());
        meta.setUsername(entity.getUsername());
        meta.setUserType(entity.getUserType());


        return meta;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = 0;
        for (Serializable id : ids) {
            result += concurrentInterceptor.invoke("cmis:workflow:from:delete-by-id:[#id]", () -> deleteById(id));
        }
        return result;
    }

    @Override
    public int deleteById(Serializable id) {
        Assert.notNull(id, "参数 id 不能为空");

        FormEntity entity = Objects.requireNonNull(get(id), "找不到 ID 为 [" + id + "] 的流程表单信息");

        return deleteByEntity(entity);
    }

    @Override
    public int deleteByEntity(Collection<FormEntity> entities, boolean errorThrow) {
        int sum = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (sum != entities.size() && errorThrow) {
            String msg = "删除实体数据为 [" + entities + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int deleteByEntity(FormEntity entity) {
        List<ApplyEntity> list = applyService
                .lambdaQuery()
                .eq(ApplyEntity::getFormId, entity.getId())
                .in(ApplyEntity::getStatus, ApplyStatusEnum.SUBMIT_STATUS.stream().map(ApplyStatusEnum::getValue).collect(Collectors.toList()))
                .list();
        applyService.deleteByEntity(list);
        return super.deleteByEntity(entity);
    }

    @Override
    public int delete(Wrapper<FormEntity> wrapper) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    public int deleteByGroupId(Integer groupId) {
        List<FormEntity> list = lambdaQuery().eq(FormEntity::getGroupId, groupId).list();
        return deleteByEntity(list);
    }
}
