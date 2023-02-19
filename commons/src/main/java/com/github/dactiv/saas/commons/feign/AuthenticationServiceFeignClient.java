package com.github.dactiv.saas.commons.feign;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.TeacherClassGradesMeta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 认证服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_AUTHENTICATION_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface AuthenticationServiceFeignClient {

    String AUTHENTICATION_INFO_DEVICE_FIELD_NAME = "device";

    /**
     * 获取最后一次认证信息
     *
     * @param userDetails 用户明细
     * @return 认证信息
     */
    @PostMapping("authentication/info/getLastByUserId")
    Map<String, Object> getLastAuthenticationInfo(@RequestBody BasicUserDetails<Integer> userDetails);

    /**
     * 获取系统用户
     *
     * @param userDetails 用户明细
     * @return 系统用户
     */
    @PostMapping("authentication/getSystemUser")
    Map<String, Object> getSystemUser(@RequestBody BasicUserDetails<Integer> userDetails);

    /**
     * 查找教师信息
     *
     * @param filter 过滤条件
     * @return 教师信息集合
     */
    @PostMapping("teacher/find")
    List<Map<String, Object>> findTeacher(@RequestBody Map<String, Object> filter);

    /**
     * 查找学生信息
     *
     * @param filter 过滤条件
     * @return 学生信息集合
     */
    @PostMapping("student/find")
    List<Map<String, Object>> findStudent(@RequestBody Map<String, Object> filter);

    /**
     * 更新教师班级信息
     *
     * @param meta 基础班级元数据信息
     * @return 响应结果
     */
    @PostMapping("teacher/updateTeacherClassGradesInfo")
    Map<String, Object> updateTeacherClassGradesInfo(@RequestBody TeacherClassGradesMeta meta);

    /**
     * 获取系统用户信息
     *
     * @param id 用户 id
     * @return 系统用户信息
     */
    @GetMapping("console/user/get")
    Map<String, Object> getConsoleUser(@RequestParam("id") Integer id);
}
