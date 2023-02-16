package com.github.dactiv.saas.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.message.domain.entity.EvaluateMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_evaluate_message 的数据访问
 *
 * <p>Table: tb_evaluate_message - 评价消息</p>
 *
 * @author maurice.chen
 * @see EvaluateMessageEntity
 * @since 2022-06-30 06:08:37
 */
@Mapper
@Repository
public interface EvaluateMessageDao extends BaseMapper<EvaluateMessageEntity> {

}
