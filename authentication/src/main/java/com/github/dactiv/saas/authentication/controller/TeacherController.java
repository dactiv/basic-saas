package com.github.dactiv.saas.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeView;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.authentication.service.TeacherService;
import com.github.dactiv.saas.commons.domain.meta.TeacherClassGradesMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_teacher 的控制器
 *
 * <p>Table: tb_teacher - 教师表</p>
 *
 * @author maurice.chen
 * @see TeacherEntity
 * @since 2022-03-07 11:19:27
 */
@RestController
@RequestMapping(TeacherController.DEFAULT_CONTROLLER_NAME)
@Plugin(
        name = "教师管理",
        id = TeacherController.DEFAULT_CONTROLLER_NAME,
        parent = "organization",
        icon = "icon-teacher-fill",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class TeacherController {

    public static final String DEFAULT_CONTROLLER_NAME = "teacher";

    private final TeacherService teacherService;

    private final AuthorizationService authorizationService;

    private final MybatisPlusQueryGenerator<TeacherEntity> queryGenerator;

    public TeacherController(MybatisPlusQueryGenerator<TeacherEntity> queryGenerator,
                             AuthorizationService authorizationService,
                             TeacherService teacherService) {
        this.teacherService = teacherService;
        this.authorizationService = authorizationService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_teacher 实体集合
     *
     * @param request http servlet request
     * @return tb_teacher 实体集合
     * @see TeacherEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<TeacherEntity> find(HttpServletRequest request) {
        QueryWrapper<TeacherEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return teacherService.find(query);
    }

    /**
     * 获取 table: tb_teacher 实体集合(前端使用)
     *
     * @return tb_teacher 实体集合
     * @see TeacherEntity
     */
    @PostMapping("findByFrontEnd")
    public List<TeacherEntity> findByFrontEnd() {
        return teacherService.lambdaQuery().eq(TeacherEntity::getPublishResource, YesOrNo.Yes.getValue()).list();
    }

    /**
     * 保存 table: tb_teacher 实体
     *
     * @param entity table: tb_teacher 实体
     * @return rest 结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[teacher:save]')")
    @Plugin(name = "保存信息", audit = true)
    public RestResult<Integer> save(@RequestBody TeacherEntity entity) {
        teacherService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 获取 table: tb_teacher 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see TeacherEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[teacher:page]')")
    public Page<TeacherEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<TeacherEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return teacherService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_teacher 分页信息
     *
     * @param pageRequest 分页信息
     * @return 分页实体
     * @see TeacherEntity
     */
    @PostMapping("pageByFrontEnd")
    public Page<TeacherEntity> pageByFrontEnd(PageRequest pageRequest) {
        Wrapper<TeacherEntity> wrapper = Wrappers
                .<TeacherEntity>lambdaQuery()
                .eq(TeacherEntity::getPublishResource, YesOrNo.Yes.getValue());
        return teacherService.findPage(pageRequest, wrapper);
    }

    /**
     * 获取 table: tb_teacher 实体
     *
     * @param id 主键 ID
     * @return tb_teacher 实体
     * @see TeacherEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[teacher:get]')")
    @Plugin(name = "编辑信息")
    public TeacherEntity get(@RequestParam Integer id) {
        return teacherService.get(id);
    }

    /**
     * 根据科目获取教师集合
     *
     * @param subject 科目值
     * @param ignore  忽略的教师 id 集合
     * @return 教师集合
     */
    @PostMapping(
            value = "findBySubject",
            headers = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_HEADER_NAME + "=" + IncludeView.ID_NAME_VIEW
    )
    @PreAuthorize("isAuthenticated()")
    public List<TeacherEntity> findBySubject(@RequestParam Integer subject,
                                             @RequestParam(required = false) List<Integer> ignore) {
        return teacherService.findBySubject(subject, ignore);
    }

    /**
     * 根据部门获取教师集合
     *
     * @param departmentId 部门 id
     * @param ignore       忽略的教师 id 集合
     * @return 教师集合
     */
    @PostMapping(
            value = "findByDepartment",
            headers = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_HEADER_NAME + "=" + IncludeView.ID_NAME_VIEW
    )
    @PreAuthorize("isAuthenticated()")
    public List<TeacherEntity> findByDepartment(@RequestParam Integer departmentId,
                                                @RequestParam(required = false) List<Integer> ignore) {
        return teacherService.findByDepartment(departmentId, ignore);
    }

    /**
     * 删除 table: tb_teacher 实体
     *
     * @param ids 主键 ID 值集合
     * @see TeacherEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[teacher:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        teacherService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 同步教师数据
     *
     * @param cipherText 密文
     * @return rest 结果集
     */
    @PostMapping("syncData")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<String> syncData(@RequestParam String cipherText) {
        return RestResult.ofSuccess("同步教师数据成功", teacherService.syncData(cipherText));
    }

    /**
     * 更新教师班级信息
     *
     * @param meta 基础班级元数据信息
     * @return 响应结果
     */
    @PostMapping("updateTeacherClassGradesInfo")
    RestResult<?> updateTeacherClassGradesInfo(@RequestBody TeacherClassGradesMeta meta) {
        teacherService.updateClassGradesInfo(meta);
        return RestResult.of("同步教师数据成功");
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
        return !teacherService
                .lambdaQuery()
                .select(TeacherEntity::getId)
                .eq(TeacherEntity::getUsername, username)
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
        return !teacherService
                .lambdaQuery()
                .select(TeacherEntity::getId)
                .eq(TeacherEntity::getPhoneNumber, phoneNumber)
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
        return !teacherService
                .lambdaQuery()
                .select(TeacherEntity::getId)
                .eq(TeacherEntity::getEmail, email)
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
    @PreAuthorize("hasAuthority('perms[teacher:update_password]') and isFullyAuthenticated()")
    @Idempotent(key = "authentication:teacher:update-password:[#securityContext.authentication.details.id]")
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        authorizationService.updatePassword(userDetails, oldPassword, newPassword);

        return RestResult.of("修改密码成功");
    }

    @PostMapping("adminUpdatePassword")
    @Plugin(name = "管理员修改登陆密码", audit = true)
    @Idempotent(key = "authentication:teacher:admin-update-password:[#id]")
    @PreAuthorize("hasAuthority('perms[teacher:admin_update_password]') and isFullyAuthenticated()")
    public RestResult<?> adminUpdatePassword(@RequestParam Integer id, @RequestParam String newPassword) {
        teacherService.adminUpdatePassword(id, newPassword);
        return RestResult.of("修改密码成功");
    }
}
