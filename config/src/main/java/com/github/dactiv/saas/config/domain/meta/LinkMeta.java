package com.github.dactiv.saas.config.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 链接元数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class LinkMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -8582444463637019308L;

    /**
     * 值
     */
    private String value;

    /**
     * 类型
     */
    private String type;
}
