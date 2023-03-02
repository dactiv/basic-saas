package com.github.dactiv.saas.workflow.domain.body.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 工作内容响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"applyId","username", "userId"})
@EqualsAndHashCode(callSuper = true)
public class WorkResponseBody extends WorkEntity {

    @Serial
    private static final long serialVersionUID = 716069114148928625L;

    /**
     * 申请信息
     */
    @JsonIgnoreProperties({
            "applyContent",
            "formName",
            "formId",
            "successTime",
            "exception",
            "executeStatus"
    })
    private ApplyEntity apply;
}
