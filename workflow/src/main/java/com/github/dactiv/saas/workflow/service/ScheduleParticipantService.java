package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.ScheduleParticipantDao;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleParticipantEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * tb_schedule_participant 的业务逻辑
 *
 * <p>Table: tb_schedule_participant - 日程参与者表</p>
 *
 * @see ScheduleParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2022-12-29 02:26:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleParticipantService extends BasicService<ScheduleParticipantDao, ScheduleParticipantEntity> {

    public List<ScheduleParticipantEntity> findByScheduleId(Integer scheduleId) {
        return lambdaQuery().eq(ScheduleParticipantEntity::getScheduleId, scheduleId).list();
    }

    public void deleteByScheduleId(Integer scheduleId) {
        lambdaUpdate().eq(ScheduleParticipantEntity::getScheduleId, scheduleId).remove();
    }
}
