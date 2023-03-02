package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.ApplyStatusEnum;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;
import java.util.Map;


/**
 * <p>Table: tb_apply - 流程申请表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@NoArgsConstructor
@Alias("apply")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_apply", autoResultMap = true)
public class ApplyEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer>, ExecuteStatus.Body {

    @Serial
    private static final long serialVersionUID = -4621173779179282756L;

    /**
     * 主键 id
     */
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 申请内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> applyContent;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 审批类型
     */
    private FormApprovalTypeEnum approvalType;

    /**
     * 表单 id
     */
    @NotNull
    private Integer formId;

    /**
     * 申请表单类型
     */
    @NotNull
    private ApplyFormTypeEnum formType = ApplyFormTypeEnum.CUSTOM;

    /**
     * 表单内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> formContent;

    /**
     * 状态:10,新创建,20.审批中,30.已通过,40.不通过
     */
    private ApplyStatusEnum status = ApplyStatusEnum.NEW;

    /**
     * 审批数量
     */
    private Integer approvalCount = 0;

    /**
     * 加急次数
     */
    private Integer urgentCount = 0;

    /**
     * 加急时间
     */
    private Date urgingTime;

    /**
     * 完成时间
     */
    private Date completionTime;

    /**
     * 撤销时间
     */
    private Date cancellationTime;

    /**
     * 发送站内信成功时间
     */
    private Date successTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 执行状态
     */
    private ExecuteStatus executeStatus;

    public CreateCustomApplyDto toCustomApplyDto() {
        CreateCustomApplyDto dto = new CreateCustomApplyDto();

        dto.setUserDetails(this);

        dto.setId(getFormId());
        dto.setType(getFormType());
        dto.setApplyMeta(getApplyContent());
        dto.setFormName(getFormName());
        dto.setApprovalType(getApprovalType());
        dto.setContentMeta(getFormContent());

        return dto;
    }
}