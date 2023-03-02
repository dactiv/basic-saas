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
import com.github.dactiv.saas.workflow.domain.entity.GroupEntity;
import com.github.dactiv.saas.workflow.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_group 的控制器
 *
 * <p>Table: tb_group - 流程组表</p>
 *
 * @author maurice.chen
 * @see GroupEntity
 * @since 2022-03-03 02:53:54
 */
@RestController
@RequestMapping("group")
@Plugin(
        name = "流程组管理",
        id = "form_group",
        parent = "workflow",
        type = ResourceType.Security,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE,
                ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE
        }
)
public class GroupController {

    private final GroupService groupService;

    private final MybatisPlusQueryGenerator<GroupEntity> queryGenerator;

    public GroupController(MybatisPlusQueryGenerator<GroupEntity> queryGenerator,
                           GroupService groupService) {
        this.groupService = groupService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_group 实体集合
     *
     * @param request http servlet request
     * @return tb_group 实体集合
     * @see GroupEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<GroupEntity> find(HttpServletRequest request, @RequestParam(defaultValue = "false") boolean body) {
        QueryWrapper<GroupEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return groupService.find(query, body);
    }

    /**
     * 获取 table: tb_group 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see GroupEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[form_group:page]')")
    public Page<GroupEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<GroupEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return groupService.findPage(pageRequest,query);
    }

    /**
     * 获取 table: tb_group 实体
     *
     * @param id 主键 ID
     * @return tb_group 实体
     * @see GroupEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[form_group:get]')")
    @Plugin(name = "编辑信息")
    public GroupEntity get(@RequestParam Integer id) {
        return groupService.get(id);
    }

    /**
     * 保存 table: tb_group 实体
     *
     * @param entity tb_group 实体
     * @see GroupEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[form_group:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody GroupEntity entity) {
        groupService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_group 实体
     *
     * @param ids 主键 ID 值集合
     * @see GroupEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[form_group:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        groupService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
