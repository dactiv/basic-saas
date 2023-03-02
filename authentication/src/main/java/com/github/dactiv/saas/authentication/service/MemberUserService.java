package com.github.dactiv.saas.authentication.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.authentication.dao.MemberUserDao;
import com.github.dactiv.saas.authentication.domain.entity.MemberUserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_member_user 的业务逻辑
 *
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @see MemberUserEntity
 *
 * @author maurice.chen
 *
 * @since 2023-03-02 11:27:48
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MemberUserService extends BasicService<MemberUserDao, MemberUserEntity> implements WechatAuthenticationService<MemberUserEntity> {

    @Override
    public MemberUserEntity getByPhoneNumber(String phoneNumber) {
        return lambdaQuery().eq(MemberUserEntity::getPhoneNumber, phoneNumber).one();
    }

    @Override
    public MemberUserEntity getByWechatOpenId(String openId) {
        return lambdaQuery().eq(MemberUserEntity::getOpenId, openId).one();
    }

    /**
     * 通过登陆账户获取教师信息
     *
     * @param username 登陆账户
     * @return 教师实体
     */
    public MemberUserEntity getByUsername(String username) {
        return lambdaQuery()
                .eq(MemberUserEntity::getUsername, username)
                .or()
                .eq(MemberUserEntity::getEmail, username)
                .or()
                .eq(MemberUserEntity::getPhoneNumber, username)
                .one();
    }
}
