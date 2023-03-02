package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.KitDao;
import com.github.dactiv.saas.workflow.domain.entity.KitEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 *
 * tb_kit 的业务逻辑
 *
 * <p>Table: tb_kit - 套件</p>
 *
 * @see KitEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:18:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KitService extends BasicService<KitDao, KitEntity> {

    public void deleteByCategoryId(Serializable id) {
        lambdaUpdate().eq(KitEntity::getCategoryId, id).remove();
    }
}
