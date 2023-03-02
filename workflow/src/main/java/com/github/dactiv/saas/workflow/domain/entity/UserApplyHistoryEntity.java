package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;
import java.util.List;


/**
 * <p>Table: tb_user_apply_history - 用户提交申请审核人历史记录</p>
 *
 * @author maurice.chen
 * @since 2022-03-04 09:42:03
 */
@Data
@NoArgsConstructor
@Alias("userApplyHistory")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_apply_history", autoResultMap = true)
public class UserApplyHistoryEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -7898404476835145200L;

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
     * 表单 id
     */
    private Integer formId;

    /**
     * 流程表单类型
     */
    private ApplyFormTypeEnum formType;

    /**
     * 流程表单名称
     */
    private String formName;

    /**
     * 审核人
     */
    @JsonCollectionGenericType(AuditParticipantMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<AuditParticipantMeta> participant;

}