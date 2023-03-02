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
import com.github.dactiv.saas.workflow.domain.entity.KitEntity;
import com.github.dactiv.saas.workflow.service.KitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_kit 的控制器
 *
 * <p>Table: tb_kit - 套件</p>
 *
 * @see KitEntity
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:18:57
 */
@RestController
@RequestMapping("kit")
@Plugin(
    name = "套件管理",
    id = "kit",
    parent = "kit_category",
    type = ResourceType.Security,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class KitController {

    private final KitService kitService;

    private final MybatisPlusQueryGenerator<KitEntity> queryGenerator;

    public KitController(MybatisPlusQueryGenerator<KitEntity> queryGenerator,
                                         KitService kitService) {
        this.kitService = kitService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_kit 实体集合
     *
     * @param request  http servlet request
     *
     * @return tb_kit 实体集合
     *
     * @see KitEntity
    */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<KitEntity> find(HttpServletRequest request) {
        QueryWrapper<KitEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return kitService.find(query);
    }

    /**
     * 获取 table: tb_kit 分页信息
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see KitEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[kit:page]')")
    public Page<KitEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<KitEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return kitService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_kit 实体
     *
     * @param id 主键 ID
     *
     * @return tb_kit 实体
     *
     * @see KitEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[kit:get]')")
    @Plugin(name = "编辑信息")
    public KitEntity get(@RequestParam Integer id) {
        return kitService.get(id);
    }

    /**
     * 保存 table: tb_kit 实体
     *
     * @param entity tb_kit 实体
     *
     * @see KitEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[kit:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody KitEntity entity) {
        kitService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_kit 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see KitEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[kit:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        kitService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
