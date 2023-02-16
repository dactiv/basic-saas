package com.github.dactiv.saas.message.domain.body.evaluate;

import com.github.dactiv.saas.message.domain.meta.EvaluateMessageAppendMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serial;

/**
 * 评价追加请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvaluateAppendRequestBody extends EvaluateMessageAppendMeta {

    @Serial
    private static final long serialVersionUID = 582033195985348986L;

    /**
     * 评价 id
     */
    @NotNull
    private Integer evaluateId;
}
