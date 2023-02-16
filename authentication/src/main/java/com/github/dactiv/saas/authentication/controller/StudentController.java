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
import com.github.dactiv.saas.authentication.domain.entity.StudentEntity;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.StudentService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


/**
 * tb_student 的控制器
 *
 * <p>Table: tb_student - 学生表</p>
 *
 * @author maurice.chen
 * @see StudentEntity
 * @since 2022-05-28 01:03:16
 */
@RestController
@RequestMapping("student")
@Plugin(
        name = "学生管理",
        id = "student",
        icon = "icon-student",
        parent = "organization",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class StudentController {

    private final StudentService studentService;

    private final AuthorizationService authorizationService;

    private final MybatisPlusQueryGenerator<StudentEntity> queryGenerator;

    public StudentController(MybatisPlusQueryGenerator<StudentEntity> queryGenerator,
                             AuthorizationService authorizationService,
                             StudentService studentService) {
        this.studentService = studentService;
        this.authorizationService = authorizationService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_student 实体集合
     *
     * @param request http servlet request
     * @return tb_student 实体集合
     * @see StudentEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<StudentEntity> find(HttpServletRequest request) {
        QueryWrapper<StudentEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return studentService.find(query);
    }

    /**
     * 获取 table: tb_student 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see StudentEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[student:page]')")
    public Page<StudentEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<StudentEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return studentService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_student 实体
     *
     * @param id 主键 ID
     * @return tb_student 实体
     * @see StudentEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[student:get]')")
    @Plugin(name = "编辑信息")
    public StudentEntity get(@RequestParam Integer id) {
        return studentService.get(id);
    }

    /**
     * 保存 table: tb_student 实体
     *
     * @param entity tb_student 实体
     * @see StudentEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[student:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody StudentEntity entity) {
        studentService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_student 实体
     *
     * @param ids 主键 ID 值集合
     * @see StudentEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[student:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        studentService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
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
        return !studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(StudentEntity::getUsername, username)
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
        return !studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(StudentEntity::getPhoneNumber, phoneNumber)
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
        return !studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(StudentEntity::getEmail, email)
                .exists();
    }

    /**
     * 判断学号是否唯一
     *
     * @param number 学号
     * @return true 是，否则 false
     */
    @GetMapping("isNumberUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isNumberUnique(@RequestParam String number) {
        return !studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(StudentEntity::getNumber, number)
                .exists();
    }

    /**
     * 判断学籍号是否唯一
     *
     * @param code 学籍号
     * @return true 是，否则 false
     */
    @GetMapping("isCodeUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isCodeUnique(@RequestParam String code) {
        return !studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(StudentEntity::getCode, code)
                .exists();
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
    @PreAuthorize("hasAuthority('perms[student:update_password]') and isFullyAuthenticated()")
    @Idempotent(key = "authentication:student:update-password:[#securityContext.authentication.details.id]")
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        authorizationService.updatePassword(userDetails, oldPassword, newPassword);

        return RestResult.of("修改密码成功");
    }

    @PostMapping("adminUpdatePassword")
    @Plugin(name = "管理员修改登陆密码", audit = true)
    @Idempotent(key = "authentication:student:admin-update-password:[#id]")
    @PreAuthorize("hasAuthority('perms[student:admin_update_password]') and isFullyAuthenticated()")
    public RestResult<?> adminUpdatePassword(@RequestParam Integer id, @RequestParam String newPassword) {
        studentService.adminUpdatePassword(id, newPassword);
        return RestResult.of("修改密码成功");
    }
}
