package com.github.dactiv.saas.workflow.domain.meta;

import com.github.dactiv.saas.commons.domain.meta.workflow.FormMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 待调度名称的表单元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduleFormMeta extends FormMeta {

    @Serial
    private static final long serialVersionUID = 6032742015464272418L;

    /**
     * 调度名称
     */
    private String scheduleName;
}
