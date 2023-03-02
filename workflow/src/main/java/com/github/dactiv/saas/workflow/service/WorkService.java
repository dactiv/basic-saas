package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.WorkDao;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_work 的业务逻辑
 *
 * <p>Table: tb_work - 工作内容表</p>
 *
 * @author maurice.chen
 * @see WorkEntity
 * @since 2022-03-03 02:31:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkService extends BasicService<WorkDao, WorkEntity> {

}
