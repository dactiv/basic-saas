package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.ApplyCopyDao;
import com.github.dactiv.saas.workflow.domain.entity.ApplyCopyEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_apply_copy 的业务逻辑
 *
 * <p>Table: tb_apply_copy - 申请抄送表</p>
 *
 * @author maurice.chen
 * @see ApplyCopyEntity
 * @since 2022-03-04 07:39:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplyCopyService extends BasicService<ApplyCopyDao, ApplyCopyEntity> {

}
