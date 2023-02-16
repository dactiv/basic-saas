package com.github.dactiv.saas.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_comment_message 的数据访问
 *
 * <p>Table: tb_comment_message - 评论消息</p>
 *
 * @author maurice.chen
 * @see CommentMessageEntity
 * @since 2022-07-01 10:59:09
 */
@Mapper
@Repository
public interface CommentMessageDao extends BaseMapper<CommentMessageEntity> {

}
