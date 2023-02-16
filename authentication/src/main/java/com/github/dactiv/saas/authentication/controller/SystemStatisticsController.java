package com.github.dactiv.saas.authentication.controller;

import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.authentication.service.SystemStatisticsService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("statistics")
public class SystemStatisticsController {

    private final SystemStatisticsService systemStatisticsService;

    public SystemStatisticsController(SystemStatisticsService systemStatisticsService) {
        this.systemStatisticsService = systemStatisticsService;
    }

    /**
     * 统计学生和教师数据
     *
     * @return 数据映射
     */
    @GetMapping("countStudentAndTeacher")
    @PreAuthorize("hasAuthority('perms[organization_statistics:count_student_and_teacher]')")
    @Plugin(name = "统计学生和教师数据", parent = "organization", sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.TEACHER_SOURCE_VALUE})
    public Map<String, Integer> countStudentAndTeacher() {
        return systemStatisticsService.countStudentAndTeacher();
    }

}
