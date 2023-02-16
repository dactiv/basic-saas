package com.github.dactiv.saas.message.domain.body.notice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 热门或非热门动态请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class HotOrNotRequestBody {

    /**
     * 热门的动态 id
     */
    private List<Integer> hot;

    /**
     * 非热门的动态 id
     */
    private List<Integer> notHot;
}
