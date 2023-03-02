package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_apply 的数据访问
 *
 * <p>Table: tb_apply - 流程申请表</p>
 *
 * @author maurice.chen
 * @see ApplyEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface ApplyDao extends BaseMapper<ApplyEntity> {

}
