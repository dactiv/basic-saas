package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.workflow.enumerate.WorkStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>Table: tb_work - 工作内容表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@Alias("work")
@NoArgsConstructor
@TableName("tb_work")
@EqualsAndHashCode(callSuper = true)
public class WorkEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 8635687871006458467L;
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
     * 工作名称
     */
    private String name;

    /**
     * 类型:10.我发起的, 20.我的代办, 30.我的经办, 40.我收到的
     */
    private WorkTypeEnum type;

    /**
     * 状态:10.待处理, 20.已处理
     */
    private WorkStatusEnum status;

    /**
     * 申请 id
     */
    private Integer applyId;

    public static WorkEntity of(ApplyEntity applyEntity, WorkTypeEnum workType) {
        WorkEntity work = new WorkEntity();

        work.setApplyId(applyEntity.getId());
        work.setName(applyEntity.getFormName());
        work.setType(workType);
        work.setUserType(applyEntity.getUserType());
        work.setUserId(applyEntity.getUserId());
        work.setUsername(applyEntity.getUsername());
        work.setStatus(WorkStatusEnum.PROCESSING);

        return work;
    }


}