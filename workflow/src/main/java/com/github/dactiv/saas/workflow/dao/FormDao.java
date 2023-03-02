package com.github.dactiv.saas.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_form 的数据访问
 *
 * <p>Table: tb_form - 流程表单表</p>
 *
 * @author maurice.chen
 * @see FormEntity
 * @since 2022-03-03 02:31:54
 */
@Mapper
@Repository
public interface FormDao extends BaseMapper<FormEntity> {

}
