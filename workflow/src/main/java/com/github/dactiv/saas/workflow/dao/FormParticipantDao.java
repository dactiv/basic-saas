package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.FormParticipantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_form_participant 的数据访问
 *
 * <p>Table: tb_form_participant - </p>
 *
 * @author maurice.chen
 * @see FormParticipantEntity
 * @since 2022-03-04 05:44:47
 */
@Mapper
@Repository
public interface FormParticipantDao extends BaseMapper<FormParticipantEntity> {

}
