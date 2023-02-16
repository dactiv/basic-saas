package com.github.dactiv.saas.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.message.domain.entity.NoticeMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_notice_message 的数据访问
 *
 * <p>Table: tb_notice_message - 公告表</p>
 *
 * @author maurice.chen
 * @see NoticeMessageEntity
 * @since 2022-03-16 03:32:05
 */
@Mapper
@Repository
public interface NoticeMessageDao extends BaseMapper<NoticeMessageEntity> {

}
