package com.github.dactiv.saas.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.KitCategoryDao;
import com.github.dactiv.saas.workflow.domain.body.KitCategoryBody;
import com.github.dactiv.saas.workflow.domain.entity.KitCategoryEntity;
import com.github.dactiv.saas.workflow.domain.entity.KitEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * tb_kit_category 的业务逻辑
 *
 * <p>Table: tb_kit_category - 套件类别</p>
 *
 * @see KitCategoryEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:49:12
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KitCategoryService extends BasicService<KitCategoryDao, KitCategoryEntity> {

    private final KitService kitService;

    public KitCategoryService(KitService kitService) {
        this.kitService = kitService;
    }

    public List<KitCategoryEntity> find(QueryWrapper<KitCategoryEntity> query, boolean body) {
        List<KitCategoryEntity> result = find(query);

        if (body) {
            return result
                    .stream()
                    .map(s -> Casts.of(s, KitCategoryBody.class))
                    .peek(s -> s.setItem(kitService.lambdaQuery().eq(KitEntity::getCategoryId, s.getId()).list()))
                    .collect(Collectors.toList());
        }

        return result;
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
        kitService.deleteByCategoryId(id);
        return super.deleteById(id);
    }
}
