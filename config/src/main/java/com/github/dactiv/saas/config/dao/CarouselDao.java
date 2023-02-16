package com.github.dactiv.saas.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.saas.config.domain.entity.CarouselEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_carousel 的数据访问
 *
 * <p>Table: tb_carousel - 轮播图</p>
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2022-10-21 05:01:52
 */
@Mapper
@Repository
public interface CarouselDao extends BaseMapper<CarouselEntity> {

}
