package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.ApplyApprovalDao;
import com.github.dactiv.saas.workflow.domain.entity.ApplyApprovalEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_apply_approval 的业务逻辑
 *
 * <p>Table: tb_apply_approval - 申请审批表</p>
 *
 * @author maurice.chen
 * @see ApplyApprovalEntity
 * @since 2022-03-03 02:31:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplyApprovalService extends BasicService<ApplyApprovalDao, ApplyApprovalEntity> {

}
