package com.github.dactiv.saas.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.dao.DepartmentDao;
import com.github.dactiv.saas.authentication.domain.body.response.DepartmentResponseBody;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.saas.authentication.resolver.DepartmentResolver;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * tb_department 的业务逻辑
 *
 * <p>Table: tb_department - 部门表</p>
 *
 * @author maurice.chen
 * @see DepartmentEntity
 * @since 2022-02-09 06:47:53
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DepartmentService extends BasicService<DepartmentDao, DepartmentEntity> {

    @Getter
    private final List<DepartmentResolver> departmentResolvers;

    public DepartmentService(ObjectProvider<DepartmentResolver> departmentResolvers) {
        this.departmentResolvers = departmentResolvers.orderedStream().toList();
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int sum = ids.stream().mapToInt(this::deleteById).sum();
        if (sum != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int deleteById(Serializable id) {
        DepartmentEntity entity = Objects.requireNonNull(get(id), "找不到 ID 为 [" + id + "] 的部门记录");

        return super.deleteById(entity);
    }

    @Override
    public int deleteByEntity(DepartmentEntity entity) {

        departmentResolvers.stream().filter(d -> d.isSupport(entity)).forEach(d -> d.postDelete(entity));

        lambdaQuery().eq(DepartmentEntity::getParentId, entity.getId()).list().forEach(this::deleteByEntity);
        return super.deleteByEntity(entity);
    }

    @Override
    public int deleteByEntity(Collection<DepartmentEntity> entities, boolean errorThrow) {
        int sum = entities.stream().mapToInt(e -> this.deleteById(e.getId())).sum();
        if (sum != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    public List<DepartmentEntity> find(QueryWrapper<DepartmentEntity> query, boolean loadUser) {
        List<DepartmentEntity> result = find(query);
        if (loadUser) {
            List<DepartmentEntity> bodies = new LinkedList<>();
            for (DepartmentEntity entity : result) {
                DepartmentResolver optional = departmentResolvers
                        .stream()
                        .filter(d -> d.isSupport(entity))
                        .findFirst()
                        .orElseThrow(() -> new SystemException("找不到部门实体[" + entity + "]的加载用户支持"));

                List<BasicUserDetails<Integer>> entities = optional.loadUser(entity);

                DepartmentResponseBody body = Casts.of(entity, DepartmentResponseBody.class);
                body.setPersonList(entities);
                bodies.add(body);
            }
            return bodies;
        }
        return result;
    }

    public void removeUser(Integer id, List<Integer> userIds) {

        DepartmentEntity entity = Objects.requireNonNull(get(id), "找不到 ID 为 [" + id + "] 的部门记录");

        DepartmentResolver optional = departmentResolvers
                .stream()
                .filter(d -> d.isSupport(entity))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到部门实体[" + entity + "]的加载用户支持"));

        optional.removeUser(entity, userIds);

        entity.setCount(entity.getCount() - userIds.size());

    }
}
