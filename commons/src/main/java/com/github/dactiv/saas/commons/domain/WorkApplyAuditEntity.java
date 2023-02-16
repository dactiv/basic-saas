package com.github.dactiv.saas.commons.domain;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;

import java.util.Date;

/**
 * 工作流申请审核实体
 */
public interface WorkApplyAuditEntity extends BasicIdentification<Integer> {

    /**
     * 获取数据状态
     *
     * @return 数据状态
     */
    DataRecordStatusEnum getDataStatus();

    /**
     * 设置数据状态
     *
     * @param status 数据状态
     */
    void setDataStatus(DataRecordStatusEnum status);

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    Date getUpdateTime();

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    void setUpdateTime(Date updateTime);

    /**
     * 获取审批状态
     *
     * @return 审批状态
     */
    ApplyStatusEnum getApplyStatus();

    /**
     * 设置审批状态
     *
     * @param applyStatus 审批状态
     */
    void setApplyStatus(ApplyStatusEnum applyStatus);

}
