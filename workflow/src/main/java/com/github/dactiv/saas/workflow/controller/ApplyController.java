package com.github.dactiv.saas.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.domain.dto.workflow.UserAuditOperationDto;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.workflow.domain.body.request.ApplyRequestBody;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.service.ApplyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_apply 的控制器
 *
 * <p>Table: tb_apply - 流程申请表</p>
 *
 * @author maurice.chen
 * @see ApplyEntity
 * @since 2022-03-03 02:53:53
 */
@RestController
@RequestMapping(ApplyController.CONTROLLER_NAME)
@Plugin(
        name = "流程申请管理",
        id = "apply",
        parent = "workflow",
        icon = "icon-file-add",
        type = ResourceType.Menu,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.MEMBER_SOURCE_VALUE
        }
)
public class ApplyController {

    public static final String CONTROLLER_NAME = "apply";

    public static final String GET_API_NAME = "get";

    private final ApplyService applyService;

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    public ApplyController(MybatisPlusQueryGenerator<?> queryGenerator,
                           ApplyService applyService) {
        this.applyService = applyService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_apply 实体集合
     *
     * @param request http servlet request
     * @return tb_apply 实体集合
     * @see ApplyEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<ApplyEntity> find(HttpServletRequest request) {
        QueryWrapper<ApplyEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return applyService.find(query);
    }

    /**
     * 获取 table: tb_apply 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see ApplyEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[apply:page]')")
    public Page<ApplyEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<ApplyEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return applyService.findPage(
                pageRequest,
                query
        );
    }

    /**
     * 获取 table: tb_apply 实体
     *
     * @param id 主键 ID
     * @return tb_apply 实体
     * @see ApplyEntity
     */
    @GetMapping(GET_API_NAME)
    @PreAuthorize("hasAuthority('perms[apply:get]')")
    @Plugin(name = "编辑信息")
    public ApplyEntity get(@RequestParam Integer id) {
        ApplyEntity entity = applyService.get(id);

        return applyService.convertApplyBody(entity);
    }

    /**
     * 保存 table: tb_apply 实体
     *
     * @param entity tb_apply 实体
     * @see ApplyEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[apply:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody ApplyRequestBody entity,
                                    @RequestParam boolean isPublish,
                                    @CurrentSecurityContext SecurityContext context) {

        SecurityUserDetails details = Casts.cast(context.getAuthentication().getDetails());
        entity.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(details));

        applyService.save(entity, isPublish);
        return RestResult.ofSuccess(isPublish ? "提交成功" : "保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_apply 实体
     *
     * @param ids 主键 ID 值集合
     * @see ApplyEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[apply:delete]')")
    @Plugin(name = "撤销信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        applyService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 审批申请
     *
     * @param id 主键 id
     * @return rest 结果集
     */
    @PostMapping("approval")
    @PreAuthorize("hasAuthority('perms[apply:approval]')")
    @Plugin(name = "审批申请", audit = true)
    public RestResult<Integer> approval(@RequestParam Integer id,
                                        @RequestParam Integer result,
                                        @RequestParam(required = false) String remark,
                                        @CurrentSecurityContext SecurityContext securityContext) throws Exception {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(securityUserDetails.getId());
        return applyService.approval(id, userId, result, remark);
    }

    /**
     * 发布申请
     *
     * @param ids 主键 id 集合
     * @param securityContext 当前上下文
     * @return rest 结果集
     */
    @PostMapping("publish")
    @PreAuthorize("hasAuthority('perms[apply:publish]')")
    @Plugin(name = "提交申请", audit = true)
    public RestResult<?> publish(@RequestParam List<Integer> ids,
                                 @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(securityUserDetails.getId());
        applyService.publish(ids, userId);
        return RestResult.of("提交申请成功");
    }

    /**
     * 删除文件信息
     *
     * @param id 主键 id
     * @param fieldName 字段名称
     * @param filename 文件名称
     *
     * @return rest 结果集
     */
    @PostMapping("deleteFileInfo")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<?> deleteFileInfo(Integer id, String fieldName, String filename) {
        applyService.deleteFileInfo(id, fieldName, filename);
        return RestResult.of("删除审批申请的 [" + fieldName + "] 字段 [" + fieldName + "] 文件成功");
    }

    /**
     * 创建自定义申请
     *
     * @param dto 自定义申请 dot
     *
     * @return rest 结果集合
     */
    @PostMapping("createCustomApply")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<Integer> createCustomApply(@RequestBody CreateCustomApplyDto dto) {
        Integer id = applyService.createCustomApply(dto);
        return RestResult.ofSuccess("创建自定义申请成功", id);
    }

    /**
     * 撤销申请
     *
     * @param ids 主键 id
     * @param securityContext 当前上下文
     *
     * @return rest 结果集
     */
    @PostMapping("cancel")
    @PreAuthorize("hasAuthority('perms[apply:cancel]')")
    @Plugin(name = "撤销申请", audit = true)
    public RestResult<?> cancel(@RequestParam List<Integer> ids, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        applyService.cancel(ids, SecurityUserDetailsConstants.toBasicUserDetails(securityUserDetails));

        return RestResult.of("撤销 " + ids.size() + " 条记录成功");
    }

    /**
     * 撤销申请
     *
     * @param dto 带业务 id 的基础用户 dto 实体
     *
     * @return rest 结果集
     */
    @PostMapping("cancelByBasicUserIdDto")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<?> cancelByBasicUserIdDto(@RequestBody UserAuditOperationDto dto) {
        return applyService.cancel(dto.getApplyId(), dto, false, false);
    }

    /**
     * 加急申请
     *
     * @param id 主键 id
     * @param securityContext 当前上下文
     *
     * @return rest 结果集
     */
    @PostMapping("urgent")
    @PreAuthorize("hasAuthority('perms[apply:urgent]')")
    @Plugin(name = "加急申请", audit = true)
    public RestResult<Integer> urgent(@RequestParam Integer id, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(securityUserDetails.getId());
        applyService.urgent(id, userId);
        return RestResult.ofSuccess("加急处理成功", id);
    }

}
