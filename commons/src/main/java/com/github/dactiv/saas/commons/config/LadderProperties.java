package com.github.dactiv.saas.commons.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 梯队统计配置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
public class LadderProperties {

    /**
     * 梯队数量
     */
    private int count = 4;

    /**
     * 基数
     */
    private int base = 5;
}
