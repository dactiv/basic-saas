package com.github.dactiv.saas.config.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.config.dao.CarouselDao;
import com.github.dactiv.saas.config.domain.entity.CarouselEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * tb_carousel 的业务逻辑
 *
 * <p>Table: tb_carousel - 轮播图</p>
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2022-10-21 05:01:52
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CarouselService extends BasicService<CarouselDao, CarouselEntity> {

    public void publish(List<Integer> ids) {
        List<CarouselEntity> carouselList = get(ids);
        carouselList.forEach(this::publish);
    }

    public void save(CarouselEntity entity, boolean publish) {

        if (Objects.nonNull(entity.getId())) {
            entity.setUpdateTime(new Date());
        }

        super.save(entity);

        if (publish) {
            this.publish(entity);
        }
    }

    public void publish(CarouselEntity entity) {

        Assert.isTrue(!DataRecordStatusEnum.PUBLISH.equals(entity.getStatus()), "ID 为 [" + entity.getId() + "] 的轮播图已经发布");

        entity.setPublishTime(new Date());
        entity.setStatus(DataRecordStatusEnum.PUBLISH);

        updateById(entity);
    }

    public void undercarriage(List<Integer> ids) {
        List<CarouselEntity> carouselList = get(ids);
        carouselList.forEach(this::undercarriage);
    }

    public void undercarriage(CarouselEntity entity) {
        Assert.isTrue(DataRecordStatusEnum.PUBLISH.equals(entity.getStatus()), "ID 为 [" + entity.getId() + "] 的轮播图非发布状态");

        entity.setPublishTime(null);
        entity.setStatus(DataRecordStatusEnum.UPDATE);
        entity.setUpdateTime(new Date());

        updateById(entity);
    }
}
