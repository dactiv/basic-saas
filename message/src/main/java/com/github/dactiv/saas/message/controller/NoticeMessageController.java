package com.github.dactiv.saas.message.controller;

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
import com.github.dactiv.saas.message.domain.body.notice.HotOrNotRequestBody;
import com.github.dactiv.saas.message.domain.entity.NoticeMessageEntity;
import com.github.dactiv.saas.message.service.NoticeMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


/**
 * tb_notice_message 的控制器
 *
 * <p>Table: tb_notice_message - 公告表</p>
 *
 * @author maurice.chen
 * @see NoticeMessageEntity
 * @since 2022-03-16 03:32:05
 */
@RestController
@RequestMapping("notice")
@Plugin(
        name = "站点动态",
        id = "notice_message",
        icon = "icon-dynamic",
        parent = "message",
        type = ResourceType.Menu,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.TEACHER_SOURCE_VALUE
        }
)
public class NoticeMessageController {

    private final NoticeMessageService noticeMessageService;

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    public NoticeMessageController(MybatisPlusQueryGenerator<?> queryGenerator,
                                   NoticeMessageService noticeMessageService) {
        this.noticeMessageService = noticeMessageService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_notice_message 实体集合
     *
     * @param request http servlet request
     * @return tb_notice_message 实体集合
     * @see NoticeMessageEntity
     */
    @PostMapping("find")
    public List<NoticeMessageEntity> find(HttpServletRequest request) {
        QueryWrapper<NoticeMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return noticeMessageService.find(query);
    }

    /**
     * 获取 table: tb_notice_message 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see NoticeMessageEntity
     */
    @PostMapping("page")
    public Page<NoticeMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {

        QueryWrapper<NoticeMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return noticeMessageService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_notice_message 实体
     *
     * @param id 主键 ID
     * @return tb_notice_message 实体
     * @see NoticeMessageEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[notice:get]')")
    @Plugin(name = "编辑信息")
    public NoticeMessageEntity get(@RequestParam Integer id) {
        return noticeMessageService.get(id);
    }

    /**
     * 获取公告明细
     *
     * @param id 公告 id
     * @return 公告实体
     */
    @GetMapping("detail")
    public NoticeMessageEntity detail(@RequestParam Integer id) {
        return noticeMessageService.detail(id);
    }

    /**
     * 保存 table: tb_notice_message 实体
     *
     * @param entity 公告请求响应体
     * @see NoticeMessageEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[notice:save]')")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody NoticeMessageEntity entity,
                                    @RequestParam(required = false, defaultValue = "false") Boolean publish,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        entity.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(userDetails));
        noticeMessageService.save(entity, publish);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_notice_message 实体
     *
     * @param ids 主键 ID 值集合
     * @see NoticeMessageEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[notice:delete]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        noticeMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 标记热门动态
     *
     * @param body 热门或非热门动态请求体
     * @return rest 结果集
     */
    @PostMapping("hotOrNot")
    @Plugin(name = "标记热门动态", audit = true)
    @PreAuthorize("hasAuthority('perms[notice:hot_or_not]')")
    public RestResult<?> hotOrNot(@RequestBody HotOrNotRequestBody body) {
        noticeMessageService.hotOrNot(body);
        return RestResult.of("标记记录为热门/非热门动态成功");
    }

    /**
     * 发布公告
     *
     * @param ids 公告 id 集合
     * @return rest 结果集
     */
    @PostMapping("publish")
    @PreAuthorize("hasAuthority('perms[notice:publish]')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<List<Integer>> publish(@RequestParam List<Integer> ids) {
        List<Integer> result = noticeMessageService.publish(ids);
        return RestResult.ofSuccess("发布 " + ids.size() + "条公告成功", result);
    }

}
