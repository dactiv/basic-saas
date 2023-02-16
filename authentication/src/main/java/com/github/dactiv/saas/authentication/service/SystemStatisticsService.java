package com.github.dactiv.saas.authentication.service;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.saas.authentication.domain.entity.StudentEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 系统统计服务
 *
 * @author maurice.chen
 */
@Service
public class SystemStatisticsService {

    private final TeacherService teacherService;

    private final StudentService studentService;

    public SystemStatisticsService(TeacherService teacherService, StudentService studentService) {
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    public Map<String, Integer> countStudentAndTeacher() {

        List<TeacherEntity> teacher = teacherService
                .lambdaQuery()
                .select(TeacherEntity::getId)
                .eq(SystemUserEntity::getStatus, UserStatus.Enabled.getValue())
                .list();

        long studentCount = studentService
                .lambdaQuery()
                .select(StudentEntity::getId)
                .eq(SystemUserEntity::getStatus, UserStatus.Enabled.getValue())
                .count();

        return Map.of(
                "在校老师", teacher.size(),
                "网课教师", (int) teacher.stream().filter(t -> YesOrNo.Yes.equals(t.getPublishResource())).count(),
                "在校学生", (int) studentCount
        );
    }
}
