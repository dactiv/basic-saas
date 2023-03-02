package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_apply_copy 的数据访问
 *
 * <p>Table: tb_apply_copy - 申请抄送表</p>
 *
 * @author maurice.chen
 * @see ApplyCopyEntity
 * @since 2022-03-04 07:39:49
 */
@Mapper
@Repository
public interface ApplyCopyDao extends BaseMapper<ApplyCopyEntity> {

}
