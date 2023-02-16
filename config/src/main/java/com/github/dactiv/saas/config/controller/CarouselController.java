package com.github.dactiv.saas.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.config.config.ApplicationConfig;
import com.github.dactiv.saas.config.domain.entity.CarouselEntity;
import com.github.dactiv.saas.config.service.CarouselService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


/**
 * tb_carousel 的控制器
 *
 * <p>Table: tb_carousel - 轮播图</p>
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2022-10-21 05:01:52
 */
@RestController
@RequestMapping("carousel")
@Plugin(
        name = "轮播图管理",
        id = "carousel",
        parent = "admin",
        icon = "icon-carousel",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class CarouselController {

    private final CarouselService carouselService;

    private final ApplicationConfig applicationConfig;

    private final MybatisPlusQueryGenerator<CarouselEntity> queryGenerator;

    public CarouselController(MybatisPlusQueryGenerator<CarouselEntity> queryGenerator,
                              CarouselService carouselService,
                              ApplicationConfig applicationConfig) {
        this.carouselService = carouselService;
        this.queryGenerator = queryGenerator;
        this.applicationConfig = applicationConfig;
    }

    /**
     * 获取 table: tb_carousel 实体集合
     *
     * @param request http servlet request
     * @return tb_carousel 实体集合
     * @see CarouselEntity
     */
    @PostMapping("findByFrontEnd")
    public List<CarouselEntity> findByFrontEnd(HttpServletRequest request) {

        QueryWrapper<CarouselEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        query.eq(CarouselEntity.STATUS_TABLE_FLED_NAME, DataRecordStatusEnum.PUBLISH.getValue());
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return carouselService.findPage(PageRequest.of(1, applicationConfig.getCarouselCount()), query).getElements();
    }

    /**
     * 获取 table: tb_carousel 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see CarouselEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[carousel:page]')")
    public Page<CarouselEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<CarouselEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return carouselService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_carousel 实体
     *
     * @param id 主键 ID
     * @return tb_carousel 实体
     * @see CarouselEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[carousel:get]')")
    @Plugin(name = "编辑信息")
    public CarouselEntity get(@RequestParam Integer id) {
        return carouselService.get(id);
    }

    /**
     * 保存 table: tb_carousel 实体
     *
     * @param entity tb_carousel 实体
     * @see CarouselEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[carousel:save]')")
    @Plugin(name = "保存或添加信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody CarouselEntity entity, @RequestParam boolean publish) {
        carouselService.save(entity, publish);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 发布轮播图
     *
     * @param ids 主键 ID 集合
     * @return rest 结果集
     */
    @PostMapping("publish")
    @PreAuthorize("hasAuthority('perms[carousel:publish]')")
    @Plugin(name = "发布信息", audit = true)
    public RestResult<?> publish(@RequestParam List<Integer> ids) {
        carouselService.publish(ids);
        return RestResult.of("发布 " + ids.size() + " 条记录成功");
    }

    /**
     * 下架轮播图
     *
     * @param ids 主键 ID 集合
     * @return rest 结果集
     */
    @PostMapping("undercarriage")
    @PreAuthorize("hasAuthority('perms[carousel:undercarriage]')")
    @Plugin(name = "下架信息", audit = true)
    public RestResult<?> undercarriage(@RequestParam List<Integer> ids) {
        carouselService.undercarriage(ids);
        return RestResult.of("下架 " + ids.size() + " 条记录成功");
    }

    /**
     * 删除 table: tb_carousel 实体
     *
     * @param ids 主键 ID 值集合
     * @see CarouselEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[carousel:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        carouselService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
