package com.github.dactiv.saas.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.message.domain.entity.LikeOrUnlikeEntity;
import com.github.dactiv.saas.message.service.LikeOrUnlikeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


/**
 * tb_like_or_unlike 的控制器
 *
 * <p>Table: tb_like_or_unlike - 点赞或非点赞记录</p>
 *
 * @author maurice.chen
 * @see LikeOrUnlikeEntity
 * @since 2022-09-08 04:14:58
 */
@RestController
@RequestMapping("like/or/unlike")
public class LikeOrUnlikeController {

    private final LikeOrUnlikeService likeOrUnlikeService;

    private final MybatisPlusQueryGenerator<LikeOrUnlikeEntity> queryGenerator;

    public LikeOrUnlikeController(MybatisPlusQueryGenerator<LikeOrUnlikeEntity> queryGenerator,
                                  LikeOrUnlikeService likeOrUnlikeService) {
        this.likeOrUnlikeService = likeOrUnlikeService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取 table: tb_like_or_unlike 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see LikeOrUnlikeEntity
     */
    @PostMapping("page")
    @PreAuthorize("isFullyAuthenticated()")
    public Page<LikeOrUnlikeEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<LikeOrUnlikeEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return likeOrUnlikeService.findPage(pageRequest, query);
    }

    /**
     * 保存 table: tb_like_or_unlike 实体
     *
     * @param entity tb_like_or_unlike 实体
     * @see LikeOrUnlikeEntity
     */
    @PostMapping("save")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Integer> save(@Valid @RequestBody LikeOrUnlikeEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        entity.setUserDetails(userDetails.toBasicUserDetails());
        likeOrUnlikeService.save(entity);
        return RestResult.ofSuccess("操作成功", entity.getId());
    }

}
