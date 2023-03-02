package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_schedule 的数据访问
 *
 * <p>Table: tb_schedule - 日程表</p>
 *
 * @author maurice.chen
 * @see ScheduleEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface ScheduleDao extends BaseMapper<ScheduleEntity> {

}
