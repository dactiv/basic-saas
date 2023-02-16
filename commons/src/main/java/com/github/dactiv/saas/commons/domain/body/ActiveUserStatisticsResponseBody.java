package com.github.dactiv.saas.commons.domain.body;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取用户数据统计
 *
 * @param <T> 用户主键 id
 * @param <C> 数据类型
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveUserStatisticsResponseBody<T, C> extends BasicUserDetails<T> {

    @Serial
    private static final long serialVersionUID = 3163622881593607777L;

    /**
     * 统计数据
     */
    private List<C> data = new ArrayList<>();

}
