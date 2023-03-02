package com.github.dactiv.saas.workflow.domain.body.response;

import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import com.github.dactiv.saas.workflow.domain.entity.GroupEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 组响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupResponseBody extends GroupEntity {
    @Serial
    private static final long serialVersionUID = -8570386851780384020L;

    /**
     * 表单集合
     */
    private List<FormEntity> formList = new LinkedList<>();
}
