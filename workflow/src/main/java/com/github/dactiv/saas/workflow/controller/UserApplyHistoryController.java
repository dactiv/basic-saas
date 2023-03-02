package com.github.dactiv.saas.workflow.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.enumeration.ApplyFormTypeEnum;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.workflow.domain.entity.UserApplyHistoryEntity;
import com.github.dactiv.saas.workflow.service.UserApplyHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * tb_user_apply_history 的控制器
 *
 * <p>Table: tb_user_apply_history - 用户提交申请审核人历史记录</p>
 *
 * @author maurice.chen
 * @see UserApplyHistoryEntity
 * @since 2022-03-04 09:42:03
 */
@RestController
@RequestMapping("user/apply/history")
@Plugin(
        name = "用户提交申请审核人历史记录",
        id = "user_apply_history",
        parent = "apply",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class UserApplyHistoryController {

    private final UserApplyHistoryService userApplyHistoryService;

    public UserApplyHistoryController(UserApplyHistoryService userApplyHistoryService) {
        this.userApplyHistoryService = userApplyHistoryService;
    }

    /**
     * 获取用户申请历史数据
     *
     * @param formId http servlet request
     * @return tb_user_apply_history 实体集合
     * @see UserApplyHistoryEntity
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("getByUniqueCondition")
    public UserApplyHistoryEntity getByUniqueCondition(@RequestParam(required = false) Integer formId,
                                                       @RequestParam Integer formType,
                                                       @CurrentSecurityContext SecurityContext securityContext) {

        SecurityUserDetails details = Casts.cast(securityContext.getAuthentication().getDetails());

        ApplyFormTypeEnum typeEnum = ValueEnumUtils.parse(formType, ApplyFormTypeEnum.class);
        return userApplyHistoryService.getByUniqueCondition(details.toBasicUserDetails(), typeEnum, formId);
    }


}
