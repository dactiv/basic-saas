package com.github.dactiv.saas.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.ConsoleUserService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 系统用户控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("console/user")
@Plugin(
        name = "系统用户管理",
        id = "console_user",
        parent = "organization",
        icon = "icon-system-user",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class ConsoleUserController {

    private final ConsoleUserService consoleUserService;

    private final AuthorizationService authorizationService;

    private final MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator;

    public ConsoleUserController(ConsoleUserService consoleUserService,
                                 AuthorizationService authorizationService,
                                 MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator) {
        this.consoleUserService = consoleUserService;
        this.authorizationService = authorizationService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 查找系统用户分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[console_user:page]')")
    @Plugin(name = "首页展示")
    public Page<ConsoleUserEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<ConsoleUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return consoleUserService.findPage(pageRequest, query);
    }

    /**
     * 查找系统用户信息
     *
     * @param request http 请求
     * @return 系统用户信息
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<ConsoleUserEntity> find(HttpServletRequest request) {
        QueryWrapper<ConsoleUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return consoleUserService.find(query);
    }

    /**
     * 获取系统用户实体
     *
     * @param id 主键值
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("hasRole('BASIC') or hasAuthority('perms[console_user:get]')")
    @Plugin(name = "编辑信息")
    public ConsoleUserEntity get(@RequestParam Integer id) {
        return consoleUserService.get(id);
    }

    /**
     * 保存系统用户实体
     *
     * @param entity 系统用户实体
     * @return 消息结果集
     */
    @PostMapping("save")
    @Plugin(name = "添加或保存信息", audit = true)
    @PreAuthorize("hasAuthority('perms[console_user:save]') and isFullyAuthenticated()")
    @Idempotent(key = "authentication:user:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> save(@Valid @RequestBody ConsoleUserEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        consoleUserService.save(entity);

        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除系统用户
     *
     * @param ids 系统用户主键 ID 集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @Plugin(name = "删除信息", audit = true)
    @PreAuthorize("hasAuthority('perms[console_user:delete]') and isFullyAuthenticated()")
    @Idempotent(key = "authentication:user:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {

        consoleUserService.deleteById(ids, false);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 更新系统用户登陆密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     */
    @PostMapping("updatePassword")
    @Plugin(name = "修改登陆密码", audit = true)
    @PreAuthorize("hasAuthority('perms[console_user:update_password]') and isFullyAuthenticated()")
    @Idempotent(key = "authentication:console:user:update-password:[#securityContext.authentication.details.id]")
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        authorizationService.updatePassword(userDetails, oldPassword, newPassword);

        return RestResult.of("修改密码成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     * @return true 是，否则 false
     */
    @GetMapping("isUsernameUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isUsernameUnique(@RequestParam String username) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getUsername, username)
                .exists();
    }

    /**
     * 判断邮件手机号码
     *
     * @param phoneNumber 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isPhoneNumberUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isPhoneNumberUnique(@RequestParam String phoneNumber) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getPhoneNumber, phoneNumber)
                .exists();
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isEmailUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isEmailUnique(@RequestParam String email) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getEmail, email)
                .exists();
    }

    @PostMapping("adminUpdatePassword")
    @Plugin(name = "管理员修改登陆密码", audit = true)
    @Idempotent(key = "authentication:console_user:admin-update-password:[#id]")
    @PreAuthorize("hasAuthority('perms[console_user:admin_update_password]') and isFullyAuthenticated()")
    public RestResult<?> adminUpdatePassword(@RequestParam Integer id, @RequestParam String newPassword) {
        consoleUserService.adminUpdatePassword(id, newPassword);
        return RestResult.of("修改密码成功");
    }
}
