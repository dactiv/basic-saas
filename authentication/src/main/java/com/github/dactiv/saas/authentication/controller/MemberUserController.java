package com.github.dactiv.saas.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.saas.authentication.service.MemberUserService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_member_user 的控制器
 *
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @see MemberUserEntity
 *
 * @author maurice.chen
 *
 * @since 2023-03-02 11:27:48
 */
@RestController
@RequestMapping("member/user")
@Plugin(
    name = "会员用户表",
    id = "member_user",
    parent = "user",
    icon = "icon-team",
    type = ResourceType.Menu,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class MemberUserController {

    private final MemberUserService memberUserService;

    private final MybatisPlusQueryGenerator<MemberUserEntity> queryGenerator;

    public MemberUserController(MybatisPlusQueryGenerator<MemberUserEntity> queryGenerator,
                                         MemberUserService memberUserService) {
        this.memberUserService = memberUserService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_member_user 实体集合
     *
     * @param request  http servlet request
     *
     * @return tb_member_user 实体集合
     *
     * @see MemberUserEntity
    */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<MemberUserEntity> find(HttpServletRequest request) {
        QueryWrapper<MemberUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return memberUserService.find(query);
    }

    /**
     * 获取 table: tb_member_user 分页信息
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see MemberUserEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[member_user:page]')")
    public Page<MemberUserEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<MemberUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return memberUserService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_member_user 实体
     *
     * @param id 主键 ID
     *
     * @return tb_member_user 实体
     *
     * @see MemberUserEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[member_user:get]')")
    @Plugin(name = "编辑信息")
    public MemberUserEntity get(@RequestParam Integer id) {
        return memberUserService.get(id);
    }

    /**
     * 保存 table: tb_member_user 实体
     *
     * @param entity tb_member_user 实体
     *
     * @see MemberUserEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[member_user:save]')")
    @Plugin(name = "保存或添加信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody MemberUserEntity entity) {
        memberUserService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_member_user 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see MemberUserEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[member_user:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        memberUserService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
