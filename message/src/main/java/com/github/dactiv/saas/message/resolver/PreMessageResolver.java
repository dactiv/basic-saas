package com.github.dactiv.saas.message.resolver;

import com.github.dactiv.framework.commons.id.BasicIdentification;

public interface PreMessageResolver<T extends BasicIdentification<Integer>> {

    /**
     * 保存前调用此方法
     *
     * @param entity 实体内容
     * @return true 继续执行保存，否则 false
     */
    default boolean preSave(T entity) {
        return true;
    }

    /**
     * 删除前，调用此方法
     *
     * @param entity 实体内容
     * @return true 继续执行删除，否则 false
     */
    default boolean preDelete(T entity) {
        return true;
    }
}
