package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 带值的 id 实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdValueMeta<T, V> extends IdEntity<T> {

    @Serial
    private static final long serialVersionUID = -8885126404039341575L;

    /**
     * 值
     */
    private V value;
}
