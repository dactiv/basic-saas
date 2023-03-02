package com.github.dactiv.saas.workflow.service.approval;

import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.domain.meta.workflow.FormMeta;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;

import java.util.List;

/**
 * 表单审批类型解析器
 *
 * @author maurice.chen
 */
public interface FormApprovalTypeResolver {

    /**
     * 是否支持表单
     *
     * @param type 审批类型
     * @return true 是，否则false
     */
    boolean support(FormApprovalTypeEnum type);

    /**
     * 创建审批流程集合
     *
     * @param body             流程申请信息
     * @param participantMetas 审批人数据
     * @return 流程申请审批流程集合
     */
    List<ApplyApprovalEntity> createApplyApproval(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas);

    /**
     * 创建抄送流程集合
     *
     * @param body             流程申请信息
     * @param participantMetas 抄送人数据
     * @return 抄送流程集合
     */
    List<ApplyCopyEntity> createApplyCopy(ApplyRequestBody body, List<AuditParticipantMeta> participantMetas);

    /**
     * 完成流程申请创建的后续处理
     *
     * @param body              流程申请信息
     * @param form              表单元数据
     * @param applyApprovalList 流程申请审批人集合
     * @param applyCopyList 流程申请抄送人集合
     */
    void applyComplete(ApplyRequestBody body, FormMeta form, List<ApplyApprovalEntity> applyApprovalList, List<ApplyCopyEntity> applyCopyList);

    /**
     * 完成流程审批的后续处理
     *
     * @param apply  审批的申请
     * @param entity 当前的审批信息
     */
    void approvalComplete(ApplyEntity apply, ApplyApprovalEntity entity);

    /**
     * 完成发布流程的后续处理
     *
     * @param entity 流程申请实体
     */
    void publishComplete(ApplyEntity entity);

    /**
     * 完成流程的所有审批的后续处理
     *
     * @param apply 流程申请实体
     * @param form  表单流程实体
     */
    void applyAllComplete(ApplyEntity apply, FormMeta form) throws Exception;
}
