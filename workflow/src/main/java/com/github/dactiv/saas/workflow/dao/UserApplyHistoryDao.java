package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.UserApplyHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_user_apply_history 的数据访问
 *
 * <p>Table: tb_user_apply_history - 用户提交申请审核人历史记录</p>
 *
 * @author maurice.chen
 * @see UserApplyHistoryEntity
 * @since 2022-03-04 09:42:03
 */
@Mapper
@Repository
public interface UserApplyHistoryDao extends BaseMapper<UserApplyHistoryEntity> {

}
