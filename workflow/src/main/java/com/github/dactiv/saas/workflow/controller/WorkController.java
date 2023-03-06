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
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.workflow.domain.entity.ApplyEntity;
import com.github.dactiv.saas.workflow.domain.entity.WorkEntity;
import com.github.dactiv.saas.workflow.enumerate.WorkStatusEnum;
import com.github.dactiv.saas.workflow.enumerate.WorkTypeEnum;
import com.github.dactiv.saas.workflow.service.ApplyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * tb_work 的控制器
 *
 * <p>Table: tb_work - 工作内容表</p>
 *
 * @author maurice.chen
 * @see WorkEntity
 * @since 2022-03-03 02:53:54
 */
@RestController
@RequestMapping("work")
public class WorkController {

    private final ApplyService applyService;

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    public WorkController(MybatisPlusQueryGenerator<?> queryGenerator,
                          ApplyService applyService) {
        this.applyService = applyService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_work 实体
     *
     * @param id 主键 ID
     * @return tb_work 实体
     * @see WorkEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[work:get]')")
    @Plugin(
            name = "查看工作内容信息",
            parent = "workflow",
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    public WorkEntity get(@RequestParam Integer id) {
        return applyService.getWorkService().get(id);
    }

    /**
     * 获取 table: tb_apply 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see ApplyEntity
     */
    @PostMapping("myCreate")
    @Plugin(
            name = "我创建的",
            parent = "workflow",
            sort = 101,
            type = ResourceType.Menu,
            icon = "icon-history",
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    @PreAuthorize("hasAuthority('perms[apply:my_create]')")
    public Page<ApplyEntity> myCreate(PageRequest pageRequest,
                                      HttpServletRequest request,
                                      @CurrentSecurityContext SecurityContext securityContext) {

        SecurityUserDetails details = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(details.getId());

        QueryWrapper<ApplyEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        query.eq(SecurityUserDetailsConstants.USER_ID_TABLE_FIELD, userId);
        query.eq(SecurityUserDetailsConstants.USER_TYPE_TABLE_FIELD, details.getType());

        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return applyService.findPage(
                pageRequest,
                query
        );
    }

    /**
     * 获取我提交的
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("mySubmit")
    @Plugin(
            name = "我提交的",
            parent = "workflow",
            sort = 100,
            icon = "icon-complete",
            type = ResourceType.Menu,
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    @PreAuthorize("hasAuthority('perms[work:my_submit]')")
    public Page<WorkEntity> mySubmit(PageRequest pageRequest,
                                     HttpServletRequest request,
                                     @CurrentSecurityContext SecurityContext securityContext) {

        QueryWrapper<WorkEntity> query = getPrincipalQueryWrapper(request, securityContext, WorkTypeEnum.CREATED);
        return convertPageElements(pageRequest, query, true, false);

    }

    /**
     * 获取抄送我的
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("copyMe")
    @Plugin(
            name = "我收到的",
            parent = "workflow",
            icon = "icon-share",
            sort = 97,
            type = ResourceType.Menu,
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    @PreAuthorize("hasAuthority('perms[work:copy_me]')")
    public Page<WorkEntity> copyMe(PageRequest pageRequest,
                                   HttpServletRequest request,
                                   @CurrentSecurityContext SecurityContext securityContext) {
        QueryWrapper<WorkEntity> query = getPrincipalQueryWrapper(request, securityContext, WorkTypeEnum.COPY);
        return convertPageElements(pageRequest, query, true, false);
    }

    /**
     * 获取我的待办
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("myPending")
    @Plugin(
            name = "我的待办",
            parent = "workflow",
            type = ResourceType.Menu,
            icon = "icon-more",
            sort = 99,
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    @PreAuthorize("hasAuthority('perms[work:my_pending]')")
    public Page<WorkEntity> myPending(PageRequest pageRequest,
                                      HttpServletRequest request,
                                      @CurrentSecurityContext SecurityContext securityContext) {

        QueryWrapper<WorkEntity> query = getPrincipalQueryWrapper(request, securityContext, WorkTypeEnum.PENDING);
        return convertPageElements(pageRequest, query, false, true);
    }

    private Page<WorkEntity> convertPageElements(PageRequest pageRequest,
                                                 QueryWrapper<WorkEntity> query,
                                                 boolean loadApproval,
                                                 boolean appendProcessingStatus) {
        if (appendProcessingStatus) {
            query.eq(RestResult.DEFAULT_STATUS_NAME, WorkStatusEnum.PROCESSING.getValue());
        }
        Page<WorkEntity> page = applyService.getWorkService().findPage(pageRequest, query);

        List<WorkEntity> list = page
                .getElements()
                .stream()
                .map(w -> applyService.convertWorkResponseBody(w, loadApproval))
                .collect(Collectors.toList());

        page.setElements(list);

        return page;
    }

    /**
     * 获取我的经办
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("myProcessed")
    @Plugin(
            name = "我的经办",
            parent = "workflow",
            type = ResourceType.Menu,
            icon = "icon-enum-major-o",
            sort = 98,
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.MEMBER_SOURCE_VALUE
            }
    )
    @PreAuthorize("hasAuthority('perms[work:my_processed]')")
    public Page<WorkEntity> myProcessed(PageRequest pageRequest,
                                        HttpServletRequest request,
                                        @CurrentSecurityContext SecurityContext securityContext) {

        QueryWrapper<WorkEntity> query = getPrincipalQueryWrapper(request, securityContext, WorkTypeEnum.PROCESSED);
        Page<WorkEntity> page = applyService.getWorkService().findPage(pageRequest, query);

        List<WorkEntity> list = page
                .getElements()
                .stream()
                .map(w -> applyService.convertWorkResponseBody(w, false))
                .collect(Collectors.toList());

        page.setElements(list);

        return page;
    }

    private QueryWrapper<WorkEntity> getPrincipalQueryWrapper(HttpServletRequest request,
                                                              SecurityContext securityContext,
                                                              WorkTypeEnum workType) {
        SecurityUserDetails details = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer userId = Casts.cast(details.getId());

        QueryWrapper<WorkEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        query.eq(SecurityUserDetailsConstants.USER_ID_TABLE_FIELD, userId);
        query.eq(SecurityUserDetailsConstants.USER_TYPE_TABLE_FIELD, details.getType());
        query.eq(MessageServiceFeignClient.Constants.TYPE_FIELD, workType.getValue());
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return query;
    }
}
