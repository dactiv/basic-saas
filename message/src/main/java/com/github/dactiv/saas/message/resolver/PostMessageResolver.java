package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.framework.commons.id.BasicIdentification;

import java.util.Map;

public interface PostMessageResolver<T extends BasicIdentification<Integer>> {

    /**
     * 保存后，调用此方法
     *
     * @param entity 实体内容
     */
    default Map<String, Object> postSave(T entity) {
        return null;
    }

    /**
     * 删除后，调用此方法
     *
     * @param entity 实体内容
     */
    default Map<String, Object> postDelete(T entity) {
        return Map.of();
    }
}
