package com.github.dactiv.saas.message.controller;

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
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.saas.message.service.SiteMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("site")
@Plugin(
        name = "站内信消息",
        id = "site",
        parent = "message",
        icon = "icon-notification",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class SiteMessageController {

    private final SiteMessageService siteMessageService;

    private final MybatisPlusQueryGenerator<SiteMessageEntity> queryGenerator;

    public SiteMessageController(SiteMessageService siteMessageService, MybatisPlusQueryGenerator<SiteMessageEntity> queryGenerator) {
        this.siteMessageService = siteMessageService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取站内信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[site:page]')")
    @Plugin(name = "首页展示")
    public Page<SiteMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<SiteMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return siteMessageService.findPage(pageRequest, query);
    }

    @PostMapping("pageByPrincipal")
    @PreAuthorize("isAuthenticated()")
    public Page<SiteMessageEntity> pageByPrincipal(@CurrentSecurityContext SecurityContext securityContext,
                                                   PageRequest pageRequest,
                                                   HttpServletRequest request) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        QueryWrapper<SiteMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        query.eq(SecurityUserDetailsConstants.USER_ID_TABLE_FIELD, userDetails.getId());
        query.eq(SecurityUserDetailsConstants.USER_TYPE_TABLE_FIELD, userDetails.getType());

        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return siteMessageService.findPage(pageRequest, query);
    }

    /**
     * 按类型分组获取站内信未读数量
     *
     * @param securityContext 安全上下文
     * @return 按类型分组的未读数量
     */
    @GetMapping("unreadQuantity")
    @PreAuthorize("hasRole('ORDINARY') or isAuthenticated()")
    public Map<Integer, Long> unreadQuantity(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return siteMessageService.countUnreadQuantity(userDetails);
    }

    /**
     * 阅读站内信
     *
     * @param types           站内信类型
     * @param securityContext 安全上下文
     * @return 消息结果集
     */
    @PostMapping("readAll")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> readAll(@RequestParam(required = false) List<Integer> types,
                                 @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageService.read(types, userDetails);
        return RestResult.of("标记所有为已读成功");
    }

    @PostMapping("deleteRead")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> deleteRead(@RequestParam(required = false) List<Integer> types,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageService.deleteRead(types, userDetails);
        return RestResult.of("删除所有已读信息成功");

    }

    @PostMapping("read")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> read(Integer id, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        siteMessageService.read(id, userDetails);

        return RestResult.of("阅读成功");
    }

    /**
     * 获取站内信消息
     *
     * @param id 站内信消息主键 ID
     * @return 站内信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[site:get]')")
    @Plugin(name = "编辑信息")
    public SiteMessageEntity get(@RequestParam Integer id) {
        return siteMessageService.get(id);
    }

    /**
     * 删除站内信消息
     *
     * @param ids 站内信消息主键 ID 值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[site:delete]') or isFullyAuthenticated()")
    @Plugin(name = "删除信息", audit = true)
    @Idempotent(key = "message:site:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageService.deleteById(ids, userDetails);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
