package com.github.dactiv.saas.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.authentication.domain.entity.StudentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_student 的数据访问
 *
 * <p>Table: tb_student - 学生表</p>
 *
 * @author maurice.chen
 * @see StudentEntity
 * @since 2022-05-28 01:03:16
 */
@Mapper
@Repository
public interface StudentDao extends BaseMapper<StudentEntity> {

}
