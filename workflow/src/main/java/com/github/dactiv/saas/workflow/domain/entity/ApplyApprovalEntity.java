package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalResultEnum;
import com.github.dactiv.saas.workflow.enumerate.ApplyApprovalStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>Table: tb_apply_approval - 申请审批表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@NoArgsConstructor
@Alias("applyApproval")
@TableName("tb_apply_approval")
@EqualsAndHashCode(callSuper = true)
public class ApplyApprovalEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 8702336583725275949L;

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
     * 申请 id
     */
    private Integer applyId;

    /**
     * 审核结果:10.通过,20.不通过
     */
    private ApplyApprovalResultEnum result;

    /**
     * 状态:10.等待审批,20.执行审批,30.审批完成, 40.无需审批
     */
    private ApplyApprovalStatusEnum status;

    /**
     * 顺序值
     */
    private Integer sort;

    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 备注
     */
    private String remark;

    public static ApplyApprovalEntity of(AuditParticipantMeta meta, Integer applyId) {

        ApplyApprovalEntity entity = new ApplyApprovalEntity();

        entity.setUserId(meta.getUserId());
        entity.setUsername(meta.getUsername());
        entity.setUserType(meta.getUserType());

        entity.setSort(meta.getSort());
        entity.setApplyId(applyId);

        return entity;
    }

}