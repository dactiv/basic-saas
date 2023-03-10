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
import com.github.dactiv.saas.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.saas.message.domain.meta.SmsBalanceMeta;
import com.github.dactiv.saas.message.service.SmsMessageService;
import com.github.dactiv.saas.message.service.support.sms.SmsChannelSender;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>短信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("sms")
@Plugin(
        name = "短信消息",
        id = "sms",
        parent = "message",
        icon = "icon-sms",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class SmsMessageController {

    private final SmsMessageService smsMessageService;

    private final List<SmsChannelSender> smsChannelSenders;

    private final MybatisPlusQueryGenerator<SmsMessageEntity> queryGenerator;

    public SmsMessageController(SmsMessageService smsMessageService,
                                List<SmsChannelSender> smsChannelSenders,
                                MybatisPlusQueryGenerator<SmsMessageEntity> queryGenerator) {
        this.smsMessageService = smsMessageService;
        this.smsChannelSenders = smsChannelSenders;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取短信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[sms:page]')")
    @Plugin(name = "首页展示", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<SmsMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<SmsMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return smsMessageService.findPage(pageRequest, query);
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     * @return 短信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[sms:get]')")
    @Plugin(name = "编辑信息", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public SmsMessageEntity get(@RequestParam Integer id) {
        return smsMessageService.get(id);
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[sms:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除信息", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:message:sms:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        smsMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return 余额实体集合
     */
    @GetMapping("balance")
    @PreAuthorize("hasAuthority('perms[sms:balance]')")
    @Plugin(name = "查看短信余额", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public List<SmsBalanceMeta> balance() {
        return smsChannelSenders.stream().map(SmsChannelSender::getBalance).toList();
    }

}
