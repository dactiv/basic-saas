package com.github.dactiv.saas.commons.feign;

import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.SystemConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 管理服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_ADMIN_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface AdminServiceFeignClient {

    /**
     * 根据学生 id 删除学生明细
     *
     * @param studentIds 学生 id
     */
    @PostMapping("student/details/deleteByStudentId")
    void deleteUserDetails(@RequestParam("studentIds") List<Integer> studentIds);

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{name:.*}")
    List<Map<String, Object>> findDataDictionaries(@PathVariable("name") String name);
}
