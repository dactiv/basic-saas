package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.KitCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_kit_category 的数据访问
 *
 * <p>Table: tb_kit_category - 套件类别</p>
 *
 * @see KitCategoryEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:49:12
 */
@Mapper
@Repository
public interface KitCategoryDao extends BaseMapper<KitCategoryEntity> {

}
