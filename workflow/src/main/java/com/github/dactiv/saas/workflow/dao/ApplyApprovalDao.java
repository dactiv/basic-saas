package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_apply_approval 的数据访问
 *
 * <p>Table: tb_apply_approval - 申请审批表</p>
 *
 * @author maurice.chen
 * @see ApplyApprovalEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface ApplyApprovalDao extends BaseMapper<ApplyApprovalEntity> {

}
