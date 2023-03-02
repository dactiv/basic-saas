package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.workflow.enumerate.ScheduleParticipantStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;

/**
 * 日程参与者
 */
@Data
@NoArgsConstructor
@Alias("schedule_participant")
@TableName(value = "tb_schedule_participant", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ScheduleParticipantEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -6473567568569891984L;

    /**
     * 主键 id
     */
    private Integer id;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 状态
     */
    private ScheduleParticipantStatusEnum status = ScheduleParticipantStatusEnum.WAITING;

    /**
     * 确认时间
     */
    private Date confirmTime;

    /**
     * 签到时间
     */
    private Date signInTime;

    /**
     * 日程 id
     */
    private Integer scheduleId;

}
