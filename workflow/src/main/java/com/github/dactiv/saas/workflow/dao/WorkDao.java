package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_work 的数据访问
 *
 * <p>Table: tb_work - 工作内容表</p>
 *
 * @author maurice.chen
 * @see WorkEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface WorkDao extends BaseMapper<WorkEntity> {

}
