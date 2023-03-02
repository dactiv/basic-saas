package com.github.dactiv.saas.workflow.service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.workflow.dao.UserApplyHistoryDao;
import com.github.dactiv.saas.workflow.domain.entity.UserApplyHistoryEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * tb_user_apply_history 的业务逻辑
 *
 * <p>Table: tb_user_apply_history - 用户提交申请审核人历史记录</p>
 *
 * @author maurice.chen
 * @see UserApplyHistoryEntity
 * @since 2022-03-04 09:42:03
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserApplyHistoryService extends BasicService<UserApplyHistoryDao, UserApplyHistoryEntity> {

    public UserApplyHistoryEntity getByUniqueCondition(TypeUserDetails<Integer> user, ApplyFormTypeEnum formType, Integer formId) {

        LambdaQueryChainWrapper<UserApplyHistoryEntity> wrapper = lambdaQuery()
                .eq(UserApplyHistoryEntity::getFormType, formType)
                .eq(UserApplyHistoryEntity::getUserId, user.getUserId())
                .eq(BasicUserDetails::getUserType, user.getUserType());

        if (Objects.nonNull(formId)) {
            wrapper = wrapper.eq(UserApplyHistoryEntity::getFormId, formId);
        }

        return wrapper.one();
    }

}
