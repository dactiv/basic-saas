package com.github.dactiv.saas.workflow.domain.body;

import com.github.dactiv.saas.workflow.domain.entity.ScheduleEntity;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleParticipantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 日程请求响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduleBody extends ScheduleEntity {

    @Serial
    private static final long serialVersionUID = 4120732175463453684L;

    /**
     * 参与者信息
     */
    private List<ScheduleParticipantEntity> participantList = new LinkedList<>();

}
