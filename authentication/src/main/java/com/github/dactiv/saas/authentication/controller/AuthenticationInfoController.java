package com.github.dactiv.saas.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.saas.authentication.service.AuthenticationInfoService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证信息控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("authentication/info")
@Plugin(
        name = "用户登陆信息",
        parent = "system",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
        type = ResourceType.Menu,
        icon = "icon-sign-out"
)
public class AuthenticationInfoController {

    private final AuthenticationInfoService authenticationInfoService;

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    public AuthenticationInfoController(AuthenticationInfoService authenticationInfoService,
                                        MybatisPlusQueryGenerator<?> queryGenerator) {
        this.authenticationInfoService = authenticationInfoService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取认证信息表分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public Page<AuthenticationInfoEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AuthenticationInfoEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return authenticationInfoService.findPage(pageRequest, query);
    }

    /**
     * 获取认证信息实体
     *
     * @param id 主键值
     * @return 认证信息实体
     */
    @GetMapping("get")
    @Plugin(name = "查看详情")
    @PreAuthorize("hasAuthority('perms[authentication_info:get]')")
    public AuthenticationInfoEntity get(@RequestParam Integer id) {
        return authenticationInfoService.get(id);
    }

    /**
     * 获取认证信息实体
     *
     * @param userDetails 用户明细
     * @return 认证信息实体
     */
    @PostMapping("getLastByUserId")
    @PreAuthorize("hasRole('BASIC')")
    public AuthenticationInfoEntity getLastByUserId(@RequestBody BasicUserDetails<Integer> userDetails) {
        return authenticationInfoService.getLastByUserDetails(userDetails);
    }

}
