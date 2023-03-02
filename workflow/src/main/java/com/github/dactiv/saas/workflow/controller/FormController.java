package com.github.dactiv.saas.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.workflow.domain.body.FormBody;
import com.github.dactiv.saas.workflow.domain.entity.FormEntity;
import com.github.dactiv.saas.workflow.enumerate.FormStatusEnum;
import com.github.dactiv.saas.workflow.service.FormService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_form 的控制器
 *
 * <p>Table: tb_form - 流程表单表</p>
 *
 * @author maurice.chen
 * @see FormEntity
 * @since 2022-03-03 02:53:54
 */
@RestController
@RequestMapping("form")
@Plugin(
        name = "流程表单管理",
        id = "form",
        parent = "com/github/dactiv/saas/workflow",
        icon = "icon-form",
        type = ResourceType.Menu,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.TEACHER_SOURCE_VALUE
        }
)
public class FormController {

    private final FormService formService;

    private final MybatisPlusQueryGenerator<FormEntity> queryGenerator;

    public FormController(MybatisPlusQueryGenerator<FormEntity> queryGenerator,
                          FormService formService) {
        this.formService = formService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_form 实体集合
     *
     * @param request http servlet request
     * @return tb_form 实体集合
     * @see FormEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<FormEntity> find(HttpServletRequest request) {
        QueryWrapper<FormEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.eq(RestResult.DEFAULT_STATUS_NAME, FormStatusEnum.NEW.getValue());
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return formService.find(query);
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
    @PreAuthorize("hasAuthority('perms[form:page]')")
    public Page<FormEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<FormEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return formService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_form 实体
     *
     * @param id 主键 ID
     * @return tb_form 实体
     * @see FormEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[form:get]')")
    @Plugin(name = "编辑信息")
    public FormEntity get(@RequestParam Integer id) {
        FormEntity entity = formService.get(id);
        return formService.convertFormBody(entity);
    }

    /**
     * 保存 table: tb_form 实体
     *
     * @param body tb_form 实体
     * @return rest 结果集
     * @see FormEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[form:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody FormBody body) {
        body.setParticipant(CollectionUtils.isNotEmpty(body.getParticipantList()) ? YesOrNo.Yes : YesOrNo.No);
        formService.save(body);
        return RestResult.ofSuccess("保存成功", body.getId());
    }

    /**
     * 删除 table: tb_form 实体
     *
     * @param ids 主键 ID 值集合
     * @see FormEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[form:delete]')")
    @Plugin(name = "删除表单", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        formService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
