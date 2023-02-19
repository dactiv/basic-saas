package com.github.dactiv.saas.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.message.domain.body.evaluate.EvaluateAppendRequestBody;
import com.github.dactiv.saas.message.domain.entity.EvaluateMessageEntity;
import com.github.dactiv.saas.message.enumerate.EvaluateMessageTypeEnum;
import com.github.dactiv.saas.message.service.EvaluateMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * tb_evaluate 的控制器
 *
 * <p>Table: tb_evaluate - 评价消息</p>
 *
 * @author maurice.chen
 * @see EvaluateMessageEntity
 * @since 2022-06-30 06:08:37
 */
@RestController
@RequestMapping("evaluate")
@Plugin(
        name = "评价消息",
        id = "evaluate",
        parent = "message",
        type = ResourceType.Menu,
        icon = "icon-excellent",
        sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.STUDENT_SOURCE_VALUE}
)
public class EvaluateMessageController {

    private final EvaluateMessageService evaluateMessageService;

    private final MybatisPlusQueryGenerator<EvaluateMessageEntity> queryGenerator;

    public EvaluateMessageController(MybatisPlusQueryGenerator<EvaluateMessageEntity> queryGenerator,
                                     EvaluateMessageService evaluateMessageService) {
        this.evaluateMessageService = evaluateMessageService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_evaluate 实体集合
     *
     * @param request http servlet request
     * @return tb_evaluate 实体集合
     * @see EvaluateMessageEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<EvaluateMessageEntity> find(HttpServletRequest request) {
        QueryWrapper<EvaluateMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return evaluateMessageService.find(query);
    }

    /**
     * 获取 table: tb_evaluate 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see EvaluateMessageEntity
     */
    @PostMapping("page")
    @Plugin(name = "首页展示", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    @PreAuthorize("hasAuthority('perms[evaluate:page]')")
    public Page<EvaluateMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<EvaluateMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return evaluateMessageService.findPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_evaluate 分页信息
     *
     * @param pageRequest 分页信息
     * @param targetId    目标 id
     * @param targetType  目标名称
     * @return 分页实体
     * @see EvaluateMessageEntity
     */
    @PostMapping("pageByFrontEnd")
    public Page<EvaluateMessageEntity> pageByFrontEnd(PageRequest pageRequest,
                                                      Integer targetId,
                                                      Integer targetType,
                                                      @RequestParam(required = false) String content,
                                                      @CurrentSecurityContext SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();

        boolean isSecurityUserAuthenticated = authentication.isAuthenticated()
                && SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass());

        boolean isCurriculumPage = Objects.nonNull(targetId) && Objects.nonNull(targetType);

        LambdaQueryWrapper<EvaluateMessageEntity> lambdaQuery = Wrappers.lambdaQuery();
        if (isCurriculumPage) {
            lambdaQuery
                    .eq(EvaluateMessageEntity::getTargetId, targetId)
                    .eq(EvaluateMessageEntity::getTargetType, targetType)
                    .orderByDesc(EvaluateMessageEntity::getId);
        } else if (isSecurityUserAuthenticated) {
            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails());
            if (Objects.isNull(userDetails)) {
                return new Page<>();
            }
            if (ResourceSourceEnum.STUDENT_SOURCE_VALUE.equals(userDetails.getType())) {
                Integer userId = Casts.cast(userDetails.getId());
                lambdaQuery.eq(EvaluateMessageEntity::getUserId, userId);
            } else {
                return new Page<>();
            }
        } else {
            return new Page<>();
        }

        if (StringUtils.isNotBlank(content)) {
            lambdaQuery.like(EvaluateMessageEntity::getContent, content);
        }

        Page<EvaluateMessageEntity> result = evaluateMessageService.findPage(pageRequest, lambdaQuery);

        result.getElements().forEach(SecurityUserDetailsConstants::convertAnonymousUser);

        if (!isSecurityUserAuthenticated || !isCurriculumPage) {
            return result;
        }

        SecurityUserDetails userDetails = Casts.cast(authentication.getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        Integer evaluateId = evaluateMessageService.getId(targetId, targetType, userId);
        if (Objects.nonNull(evaluateId)) {
            result.setMeta(Map.of(IntegerIdEntity.ID_FIELD_NAME, evaluateId));
        }

        return result;
    }

    /**
     * 追加评论
     *
     * @param body            评价追加请求体
     * @param securityContext spring 安全上下文
     * @return rest 结果集
     */
    @PostMapping("append")
    @PreAuthorize("isAuthenticated()")
    public RestResult<String> append(@Valid @RequestBody EvaluateAppendRequestBody body,
                                     @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        evaluateMessageService.saveAppend(body, userDetails);
        return RestResult.ofSuccess("追加评价成功", body.getId());
    }

    /**
     * 删除追加评价
     *
     * @param id              评价 id
     * @param appendId        追加评价 id
     * @param securityContext spring 安全上下文
     * @return rest 结果集
     */
    @PostMapping("deleteAppend")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> deleteAppend(@RequestParam Integer id,
                                      @RequestParam String appendId,
                                      @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        evaluateMessageService.deleteAppend(id, appendId, userDetails);
        return RestResult.of("追加评价成功");
    }

    /**
     * 获取 table: tb_evaluate 实体
     *
     * @param id 主键 ID
     * @return tb_evaluate 实体
     * @see EvaluateMessageEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[evaluate:get]')")
    @Plugin(name = "编辑信息")
    public EvaluateMessageEntity get(@RequestParam Integer id) {
        return evaluateMessageService.get(id);
    }

    /**
     * 保存 table: tb_evaluate 实体
     *
     * @param entity tb_evaluate 实体
     * @see EvaluateMessageEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[evaluate:save]') or hasRole('STUDENT')")
    @Idempotent("dactiv:saas:message:evaluate:save:[#entity.userId]-[#entity.targetId]-[#entity.targetType.value]")
    @Plugin(name = "新增或修改信息", audit = true)
    public RestResult<Integer> save(@Valid @RequestBody EvaluateMessageEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        entity.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(userDetails));
        evaluateMessageService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_evaluate 实体
     *
     * @param ids 主键 ID 值集合
     * @see EvaluateMessageEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[evaluate:delete]') or hasRole('STUDENT')")
    @Plugin(name = "删除信息", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        evaluateMessageService.deleteById(ids, userDetails);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 统计摘要
     *
     * @param targetId   目标 id
     * @param targetType 目标类型
     * @return 浮点型统计摘要
     */
    @GetMapping("summaryStatistics")
    public DoubleSummaryStatistics summaryStatistics(Integer targetId, Integer targetType) {
        EvaluateMessageTypeEnum type = ValueEnumUtils.parse(targetType, EvaluateMessageTypeEnum.class);
        return evaluateMessageService.summaryStatistics(targetId, type);
    }

}
