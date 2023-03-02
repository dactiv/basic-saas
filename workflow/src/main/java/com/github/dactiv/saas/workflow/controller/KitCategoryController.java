package com.github.dactiv.saas.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.workflow.domain.entity.KitCategoryEntity;
import com.github.dactiv.saas.workflow.service.KitCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_kit_category 的控制器
 *
 * <p>Table: tb_kit_category - 套件类别</p>
 *
 * @see KitCategoryEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:49:12
 */
@RestController
@RequestMapping("kit/category")
@Plugin(
    name = "套件类别",
    id = "kit_category",
    parent = "form",
    type = ResourceType.Security,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class KitCategoryController {

    private final KitCategoryService kitCategoryService;

    private final MybatisPlusQueryGenerator<KitCategoryEntity> queryGenerator;

    public KitCategoryController(MybatisPlusQueryGenerator<KitCategoryEntity> queryGenerator,
                                         KitCategoryService kitCategoryService) {
        this.kitCategoryService = kitCategoryService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_kit_category 实体集合
     *
     * @param request  http servlet request
     *
     * @return tb_kit_category 实体集合
     *
     * @see KitCategoryEntity
    */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<KitCategoryEntity> find(HttpServletRequest request, @RequestParam(defaultValue = "false") boolean body) {
        QueryWrapper<KitCategoryEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return kitCategoryService.find(query, body);
    }

    /**
     * 获取 table: tb_kit_category 分页信息
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see KitCategoryEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[kit_category:page]')")
    public Page<KitCategoryEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<KitCategoryEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return kitCategoryService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_kit_category 实体
     *
     * @param id 主键 ID
     *
     * @return tb_kit_category 实体
     *
     * @see KitCategoryEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[kit_category:get]')")
    @Plugin(name = "编辑信息")
    public KitCategoryEntity get(@RequestParam Integer id) {
        return kitCategoryService.get(id);
    }

    /**
     * 保存 table: tb_kit_category 实体
     *
     * @param entity tb_kit_category 实体
     *
     * @see KitCategoryEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[kit_category:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody KitCategoryEntity entity) {
        kitCategoryService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_kit_category 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see KitCategoryEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[kit_category:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        kitCategoryService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
