package com.github.dactiv.saas.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_department 的数据访问
 *
 * <p>Table: tb_department - 部门表</p>
 *
 * @author maurice.chen
 * @see DepartmentEntity
 * @since 2022-02-09 06:47:53
 */
@Mapper
@Repository
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {

}
