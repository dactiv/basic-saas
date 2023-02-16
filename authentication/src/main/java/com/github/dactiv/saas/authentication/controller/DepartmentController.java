package com.github.dactiv.saas.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.saas.authentication.service.DepartmentService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * tb_department 的控制器
 *
 * <p>Table: tb_department - 部门表</p>
 *
 * @author maurice.chen
 * @see DepartmentEntity
 * @since 2022-02-09 06:47:53
 */
@RestController
@RequestMapping("department")
@Plugin(
        name = "部门管理",
        id = "department",
        parent = "organization",
        icon = "icon-department",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MybatisPlusQueryGenerator<?> queryGenerator;

    /**
     * 获取 table: tb_department 信息
     *
     * @param request http servlet request
     * @return tb_department 实体集合
     * @see DepartmentEntity
     */
    @PostMapping("find")
    @Plugin(name = "首页展示")
    @PreAuthorize("isAuthenticated()")
    public List<DepartmentEntity> find(HttpServletRequest request,
                                       @RequestParam(defaultValue = "false") boolean mergeTree,
                                       @RequestParam(defaultValue = "false") boolean loadUser) {
        QueryWrapper<DepartmentEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        List<DepartmentEntity> result = departmentService.find(query, loadUser);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(result);
        } else {
            return result;
        }
    }

    /**
     * 获取 table: tb_department 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see DepartmentEntity
     */
    @PostMapping("page")
    public Page<DepartmentEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<DepartmentEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return departmentService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_department 实体
     *
     * @param id 主键 ID
     * @return tb_department 实体
     * @see DepartmentEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[department:get]')")
    @Plugin(name = "编辑信息")
    public DepartmentEntity get(@RequestParam Integer id) {
        return departmentService.get(id);
    }

    /**
     * 保存 table: tb_department 实体
     *
     * @param entity tb_department 实体
     * @see DepartmentEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[department:save]')")
    @Plugin(name = "添加或保存信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody DepartmentEntity entity) {
        departmentService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_department 实体
     *
     * @param ids 主键 ID 值集合
     * @see DepartmentEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[department:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        departmentService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 移除用户
     *
     * @param id      部门 id
     * @param userIds 用户 id 集合
     * @return rest 结果集
     */
    @PostMapping("removeUser")
    @PreAuthorize("hasAuthority('perms[department:remove_user]')")
    @Plugin(name = "移除用户", audit = true)
    public RestResult<?> removeUser(@RequestParam Integer id, @RequestParam List<Integer> userIds) {
        departmentService.removeUser(id, userIds);
        return RestResult.of("移除" + userIds.size() + "个成员成功");
    }

    /**
     * 判断邮件是否唯一
     *
     * @param name 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isNameUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isEmailUnique(@RequestParam String name) {
        return !departmentService
                .lambdaQuery()
                .select(DepartmentEntity::getId)
                .eq(DepartmentEntity::getName, name)
                .exists();
    }

}
