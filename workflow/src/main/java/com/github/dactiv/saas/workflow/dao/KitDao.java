package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.KitEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_kit 的数据访问
 *
 * <p>Table: tb_kit - 套件</p>
 *
 * @see KitEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:18:57
 */
@Mapper
@Repository
public interface KitDao extends BaseMapper<KitEntity> {

}
