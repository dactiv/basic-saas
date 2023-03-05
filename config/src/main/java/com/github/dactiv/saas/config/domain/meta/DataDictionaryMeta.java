package com.github.dactiv.saas.config.domain.meta;

import com.github.dactiv.saas.config.enumerate.ValueTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据字典元数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class DataDictionaryMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -6880817354929730676L;

    /**
     * 名称
     */
    @NotNull
    @Length(max = 64)
    private String name;

    /**
     * 值
     */
    @NotNull
    private Object value;

    /**
     * 值类型
     */
    @NotNull
    private ValueTypeEnum valueType;

    /**
     * 等级
     */
    private String level;
}
