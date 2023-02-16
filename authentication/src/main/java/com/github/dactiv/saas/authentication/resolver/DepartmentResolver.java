package com.github.dactiv.saas.authentication.resolver;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;

import java.util.List;

/**
 * 部门解析器
 *
 * @author maurice.chen
 */
public interface DepartmentResolver {

    /**
     * 是否支持
     *
     * @param entity 部门实体
     * @return true 是，否则 false
     */
    boolean isSupport(DepartmentEntity entity);

    /**
     * 加载部门用户
     *
     * @param entity 部门实体
     * @return 部门用户集合
     */
    List<BasicUserDetails<Integer>> loadUser(DepartmentEntity entity);

    /**
     * 删除部门的后续处理
     *
     * @param entity 部门实体
     */
    void postDelete(DepartmentEntity entity);

    /**
     * 移除用户
     *
     * @param entity  部门实体
     * @param userIds 被移除的用户 id
     */
    void removeUser(DepartmentEntity entity, List<Integer> userIds);

    /**
     * 统计人数
     *
     * @param entity 部门实体
     * @return 人数
     */
    int countPerson(DepartmentEntity entity);
}
