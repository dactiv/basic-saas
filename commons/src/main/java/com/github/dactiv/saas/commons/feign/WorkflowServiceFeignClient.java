package com.github.dactiv.saas.commons.feign;


import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.domain.dto.workflow.CreateCustomApplyDto;
import com.github.dactiv.saas.commons.domain.dto.workflow.UserAuditOperationDto;
import com.github.dactiv.saas.commons.SystemConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 工作流服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = SystemConstants.SYS_WORKFLOW_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface WorkflowServiceFeignClient {

    /**
     * 删除流程申请的文件信息
     *
     * @param id        流程申请 id
     * @param fieldName 字段名称
     * @param filename  文件名称
     * @return rest 结果集
     */
    @PostMapping("apply/deleteFileInfo")
    RestResult<?> deleteApplyFileInfo(@RequestParam("id") Integer id,
                                      @RequestParam("fieldName") String fieldName,
                                      @RequestParam("filename") String filename);

    /**
     * 构造自定义申请信息
     *
     * @param dto 工作流审核元数据信息
     * @return rest 结果集
     */
    @PostMapping("apply/createCustomApply")
    RestResult<Integer> createCustomApply(@RequestBody CreateCustomApplyDto dto);

    /**
     * 撤销审核
     *
     * @param dto 主键 id
     * @return rest 结果集
     */
    @PostMapping("apply/cancelByBasicUserIdDto")
    RestResult<Integer> cancelApply(@RequestBody UserAuditOperationDto dto);
}
