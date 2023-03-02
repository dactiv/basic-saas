package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.workflow.dao.FormParticipantDao;
import com.github.dactiv.saas.workflow.domain.entity.FormParticipantEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_form_participant 的业务逻辑
 *
 * <p>Table: tb_form_participant - </p>
 *
 * @author maurice.chen
 * @see FormParticipantEntity
 * @since 2022-03-04 05:44:47
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FormParticipantService extends BasicService<FormParticipantDao, FormParticipantEntity> {

}
