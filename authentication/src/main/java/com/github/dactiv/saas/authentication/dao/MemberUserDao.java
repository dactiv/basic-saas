package com.github.dactiv.saas.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.authentication.domain.entity.MemberUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_member_user 的数据访问
 *
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @see MemberUserEntity
 *
 * @author maurice.chen
 *
 * @since 2023-03-02 11:27:48
 */
@Mapper
@Repository
public interface MemberUserDao extends BaseMapper<MemberUserEntity> {

}
