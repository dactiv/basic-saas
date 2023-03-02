package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.GroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_group 的数据访问
 *
 * <p>Table: tb_group - 流程组表</p>
 *
 * @author maurice.chen
 * @see GroupEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface GroupDao extends BaseMapper<GroupEntity> {

}
