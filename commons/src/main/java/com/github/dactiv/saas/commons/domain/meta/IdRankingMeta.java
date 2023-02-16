package com.github.dactiv.saas.commons.domain.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * id 名次元数据信息
 *
 * @param <PK> id 类型
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = false)
public class IdRankingMeta<PK> implements Serializable {

    @Serial
    private static final long serialVersionUID = 6686664513905784823L;

    /**
     * 主键 id
     */
    private PK id;

    /**
     * 排名
     */
    private Integer ranking;
}
