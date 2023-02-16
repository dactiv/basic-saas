package com.github.dactiv.saas.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_like_or_unlike 的数据访问
 *
 * <p>Table: tb_like_or_unlike - 点赞或非点赞记录</p>
 *
 * @author maurice.chen
 * @see LikeOrUnlikeEntity
 * @since 2022-09-08 04:14:58
 */
@Mapper
@Repository
public interface LikeOrUnlikeDao extends BaseMapper<LikeOrUnlikeEntity> {

}
