package com.github.dactiv.saas.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.github.dactiv.saas.message.domain.entity.CommentMessageEntity;
import com.github.dactiv.saas.message.service.CommentMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_comment_message 的控制器
 *
 * <p>Table: tb_comment_message - 评论消息</p>
 *
 * @author maurice.chen
 * @see CommentMessageEntity
 * @since 2022-07-01 10:59:09
 */
@RestController
@RequestMapping("comment")
@Plugin(
        name = "评论管理",
        id = "comment",
        parent = "message",
        icon = "icon-comment",
        type = ResourceType.Menu,
        sources = {
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE,
                ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE
        }
)
public class CommentMessageController {

    private final CommentMessageService commentMessageService;

    private final MybatisPlusQueryGenerator<CommentMessageEntity> queryGenerator;

    public CommentMessageController(MybatisPlusQueryGenerator<CommentMessageEntity> queryGenerator,
                                    CommentMessageService commentMessageService) {
        this.commentMessageService = commentMessageService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_comment_message 实体集合
     *
     * @param request http servlet request
     * @return tb_comment_message 实体集合
     * @see CommentMessageEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<CommentMessageEntity> find(HttpServletRequest request) {
        QueryWrapper<CommentMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        List<CommentMessageEntity> result = commentMessageService.find(query);
        result.forEach(commentMessageService::loadChildren);
        return result;
    }

    /**
     * 获取 table: tb_comment_message 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see CommentMessageEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[comment_message:page]')")
    @Plugin(name = "首页展示", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<CommentMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<CommentMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        Page<CommentMessageEntity> result = commentMessageService.findPage(pageRequest, query);
        result.getElements().forEach(commentMessageService::loadChildren);
        return result;
    }

    @PostMapping("pageByUserCenter")
    @PreAuthorize("hasRole('STUDENT')")
    public Page<CommentMessageEntity> pageByUserCenter(PageRequest pageRequest,
                                                       HttpServletRequest request,
                                                       @CurrentSecurityContext SecurityContext securityContext) {
        QueryWrapper<CommentMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        query.eq(SecurityUserDetailsConstants.USER_ID_TABLE_FIELD, userDetails.getId());
        query.eq(SecurityUserDetailsConstants.USER_TYPE_TABLE_FIELD, userDetails.getType());
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        Page<CommentMessageEntity> result = commentMessageService.findPage(pageRequest, query);
        result.getElements().forEach(commentMessageService::loadChildren);
        return result;
    }

    /**
     * 获取 table: tb_comment_message 分页信息
     *
     * @param pageRequest 分页信息
     * @return 分页实体
     * @see CommentMessageEntity
     */
    @PostMapping("pageByFrontEnd")
    public Page<CommentMessageEntity> pageByFrontEnd(PageRequest pageRequest,
                                                     @RequestParam Integer targetId,
                                                     @RequestParam String targetType,
                                                     @RequestParam(required = false, defaultValue = "false") Boolean isAsc) {

        LambdaQueryWrapper<CommentMessageEntity> query = Wrappers
                .<CommentMessageEntity>lambdaQuery()
                .eq(CommentMessageEntity::getTargetId, targetId)
                .eq(CommentMessageEntity::getTargetType, targetType);

        if (isAsc) {
            query.orderByAsc(CommentMessageEntity::getId);
        } else {
            query.orderByDesc(CommentMessageEntity::getId);
        }

        Page<CommentMessageEntity> result = commentMessageService.findPage(pageRequest, query);

        result.getElements().stream().peek(SecurityUserDetailsConstants::convertAnonymousUser).forEach(commentMessageService::loadChildren);

        return result;
    }

    /**
     * 获取 table: tb_comment_message 实体
     *
     * @param id 主键 ID
     * @return tb_comment_message 实体
     * @see CommentMessageEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[comment_message:get]')")
    @Plugin(name = "编辑信息")
    public CommentMessageEntity get(@RequestParam Integer id) {
        CommentMessageEntity result = commentMessageService.get(id);
        commentMessageService.loadChildren(result);
        return result;
    }

    /**
     * 保存 table: tb_comment_message 实体
     *
     * @param entity tb_comment_message 实体
     * @see CommentMessageEntity
     */
    @PostMapping("save")
    @Plugin(name = "新增或修改信息", audit = true)
    @PreAuthorize("hasAuthority('perms[comment_message:save]') or hasRole('STUDENT')")
    @Idempotent("dactiv:saas:message:comment:save:[#entity.userId]-[#entity.targetId]-[#entity.targetType]")
    public RestResult<Integer> save(@Valid @RequestBody CommentMessageEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        entity.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(userDetails));
        commentMessageService.save(entity);
        return RestResult.ofSuccess("评论成功", entity.getId());
    }

    /**
     * 删除 table: tb_comment_message 实体
     *
     * @param ids 主键 ID 值集合
     * @see CommentMessageEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[comment_message:delete]') or hasRole('STUDENT')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        commentMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
