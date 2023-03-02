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
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.workflow.domain.body.ScheduleBody;
import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleEntity;
import com.github.dactiv.saas.workflow.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * tb_schedule 的控制器
 *
 * <p>Table: tb_schedule - 日程表</p>
 *
 * @author maurice.chen
 * @see ScheduleEntity
 * @since 2022-03-03 02:53:54
 */
@RestController
@RequestMapping("schedule")
@Plugin(
        name = "日程管理",
        id = "schedule",
        parent = "workflow",
        type = ResourceType.Menu,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE,
                ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE
        }
)
public class ScheduleController {


    private final ScheduleService scheduleService;
    private final MybatisPlusQueryGenerator<ScheduleEntity> queryGenerator;

    public ScheduleController(MybatisPlusQueryGenerator<ScheduleEntity> queryGenerator,
                              ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_form 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see FormEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[schedule:page]')")
    public Page<ScheduleEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<ScheduleEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return scheduleService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_schedule 实体集合
     *
     * @param request http servlet request
     * @return tb_schedule 实体集合
     * @see ScheduleEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<ScheduleEntity> find(HttpServletRequest request) {
        return scheduleService.find(queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取 table: tb_schedule 实体
     *
     * @param id 主键 ID
     * @return tb_schedule 实体
     * @see ScheduleEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[schedule:get]')")
    @Plugin(name = "编辑信息")
    public ScheduleEntity get(@RequestParam Integer id) {
        ScheduleEntity entity = scheduleService.get(id);
        return scheduleService.convertScheduleBody(entity);
    }

    /**
     * 保存 table: tb_schedule 实体
     *
     * @param entity tb_schedule 实体
     * @see ScheduleEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[schedule:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody ScheduleBody entity,
                                    @RequestParam(defaultValue = "false") Boolean publish,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails details = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(details.getId());

        entity.setUserId(userId);
        entity.setUsername(details.getUsername());
        entity.setUserType(details.getType());

        scheduleService.save(entity, publish);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 发布信息
     *
     * @param ids 主键 id 集合
     *
     * @return rest 结果集
     */
    @PostMapping("publish")
    @PreAuthorize("hasAuthority('perms[schedule:publish]')")
    @Plugin(name = "发布信息", audit = true)
    public RestResult<?> publish(@RequestParam List<Integer> ids, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails details = Casts.cast(securityContext.getAuthentication().getDetails());
        scheduleService.publish(ids, details);
        return RestResult.of("发布成功");
    }

    /**
     * 删除 table: tb_schedule 实体
     *
     * @param ids 主键 ID 值集合
     * @see ScheduleEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[schedule:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        scheduleService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取节假日
     *
     * @param year 年份
     *
     * @return rest 结果集
     */
    @GetMapping("holiday")
    @PreAuthorize("isAuthenticated()")
    public RestResult<List<Map<String, Object>>> holiday(@RequestParam Integer year) {
        return scheduleService.getHoliday(year);
    }
}
