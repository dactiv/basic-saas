package com.github.dactiv.saas.workflow.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.GroupDao;
import com.github.dactiv.saas.workflow.domain.body.response.GroupResponseBody;
import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import com.github.dactiv.saas.workflow.domain.entity.GroupEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * tb_group 的业务逻辑
 *
 * <p>Table: tb_group - 流程组表</p>
 *
 * @author maurice.chen
 * @see GroupEntity
 * @since 2022-03-03 02:31:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService extends BasicService<GroupDao, GroupEntity> {

    private final FormService formService;

    public GroupService(FormService formService) {
        this.formService = formService;
    }

    public List<GroupEntity> find(QueryWrapper<GroupEntity> query, boolean body) {
        List<GroupEntity> result = find(query);
        if (body) {
            return result
                    .stream()
                    .map(s -> Casts.of(s, GroupResponseBody.class))
                    .peek(g -> g.setFormList(formService.lambdaQuery().eq(FormEntity::getGroupId, g.getId()).list()))
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteById(Serializable id) {
        return super.deleteById(id);
    }

    @Override
    public int deleteByEntity(Collection<GroupEntity> entities, boolean errorThrow) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteByEntity(GroupEntity entity) {
        formService.deleteByGroupId(entity.getId());
        return super.deleteByEntity(entity);
    }

    @Override
    public int delete(Wrapper<GroupEntity> wrapper) {
        throw new UnsupportedOperationException("不支持此操作");
    }
}
