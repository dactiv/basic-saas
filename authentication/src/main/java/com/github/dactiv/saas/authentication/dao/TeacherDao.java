package com.github.dactiv.saas.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_teacher 的数据访问
 *
 * <p>Table: tb_teacher - 教师表</p>
 *
 * @author maurice.chen
 * @see TeacherEntity
 * @since 2022-03-07 11:19:27
 */
@Mapper
@Repository
public interface TeacherDao extends BaseMapper<TeacherEntity> {

}
