package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.workflow.enumerate.ApplyCopyStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>Table: tb_apply_copy - 申请抄送表</p>
 *
 * @author maurice.chen
 * @since 2022-03-04 07:39:49
 */
@Data
@NoArgsConstructor
@Alias("applyCopy")
@TableName("tb_apply_copy")
@EqualsAndHashCode(callSuper = true)
public class ApplyCopyEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 7556502708113057531L;

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
     * 状态:10.等待抄送,20.已抄送,30.无需抄送
     */
    private ApplyCopyStatusEnum status;

    /**
     * 顺序值
     */
    private Integer sort;

    /**
     * 抄送时间
     */
    private Date copyTime;

    public static ApplyCopyEntity of(AuditParticipantMeta meta, Integer applyId) {

        ApplyCopyEntity entity = new ApplyCopyEntity();

        entity.setUserId(meta.getUserId());
        entity.setUsername(meta.getUsername());
        entity.setUserType(meta.getUserType());

        entity.setSort(meta.getSort());
        entity.setApplyId(applyId);

        return entity;
    }


}