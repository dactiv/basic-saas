package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.enumeration.FormApprovalTypeEnum;
import com.github.dactiv.saas.workflow.domain.meta.ScheduleFormMeta;
import com.github.dactiv.saas.workflow.enumerate.FormStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.FormTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_form - 流程表单表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@Alias("form")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_form", autoResultMap = true)
public class FormEntity extends ScheduleFormMeta implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -766165470606724845L;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 可以发起审批的部门
     */
    @JsonCollectionGenericType(IdNameMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdNameMeta> applyDepartment;

    /**
     * 审批方式:10,顺序审批, 20,会签，30.或签
     */
    private FormApprovalTypeEnum approvalType;

    /**
     * 类型分组
     */
    private Integer groupId;

    /**
     * 类型分组名称
     */
    private String groupName;

    /**
     * 表单设计内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> design;

    /**
     * 审批图标
     */
    private String icon;

    /**
     * 状态:10.待发布，20.已发布，30.已作废
     */
    private FormStatusEnum status;

    /**
     * 表单类型: 10 系统表单, 20.自定义表单
     */
    private FormTypeEnum type;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否存在参与者:0.否,1.是
     */
    private YesOrNo participant;

}