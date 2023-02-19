package com.github.dactiv.saas.message.domain.meta;

import com.github.dactiv.framework.commons.id.StringIdEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

/**
 * 评价消息追加内容元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvaluateMessageAppendMeta extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = 8342135365088270616L;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 评价内容
     */
    @NotNull
    private String content;

    /**
     * 更新时间
     */
    private Date updateTime;
}
