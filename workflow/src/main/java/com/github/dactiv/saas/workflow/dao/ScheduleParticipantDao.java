package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleParticipantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_schedule_participant 的数据访问
 *
 * <p>Table: tb_schedule_participant - 日程参与者表</p>
 *
 * @see ScheduleParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2022-12-29 02:26:11
 */
@Mapper
@Repository
public interface ScheduleParticipantDao extends BaseMapper<ScheduleParticipantEntity> {

}
