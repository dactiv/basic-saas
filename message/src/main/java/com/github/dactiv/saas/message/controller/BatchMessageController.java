package com.github.dactiv.saas.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.message.domain.entity.BatchMessageEntity;
import com.github.dactiv.saas.message.service.BatchMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * tb_batch_message 的控制器
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @see BatchMessageEntity
 * @since 2021-08-22 04:45:14
 */
@RestController
@RequestMapping("batch")
@Plugin(
        name = "批量消息",
        id = "batch",
        parent = "message",
        icon = "icon-batch",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class BatchMessageController {

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    private final BatchMessageService batchMessageService;

    public BatchMessageController(MybatisPlusQueryGenerator<?> queryGenerator,
                                  BatchMessageService batchMessageService) {
        this.queryGenerator = queryGenerator;
        this.batchMessageService = batchMessageService;
    }

    /**
     * 获取 table: tb_batch_message 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see BatchMessageEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[batch_message:page]')")
    public Page<BatchMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<BatchMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return batchMessageService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_batch_message 实体
     *
     * @param id 主键 ID
     * @return tb_batch_message 实体
     * @see BatchMessageEntity
     */
    @GetMapping("get")
    @Plugin(name = "编辑信息")
    @PreAuthorize("hasAuthority('perms[batch_message:get]')")
    public BatchMessageEntity get(@RequestParam("id") Integer id) {
        return batchMessageService.get(id);
    }

    /**
     * 删除 table: tb_batch_message 实体
     *
     * @param ids 主键 ID 值集合
     * @see BatchMessageEntity
     */
    @PostMapping("delete")
    @Plugin(name = "删除信息", audit = true)
    @PreAuthorize("hasAuthority('perms[batch_message:delete]')")
    @Idempotent(key = "message:email:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        batchMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
