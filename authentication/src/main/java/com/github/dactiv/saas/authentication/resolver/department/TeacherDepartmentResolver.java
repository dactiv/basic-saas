package com.github.dactiv.saas.authentication.resolver.department;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.enumerate.DepartmentTypeEnum;
import com.github.dactiv.saas.authentication.resolver.DepartmentResolver;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import com.github.dactiv.saas.authentication.service.TeacherService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师部门解析器实现
 *
 * @author maurice.chen
 */
@Component
public class TeacherDepartmentResolver implements DepartmentResolver {

    private final TeacherService teacherService;

    private final MybatisPlusQueryGenerator<TeacherEntity> queryGenerator;

    public TeacherDepartmentResolver(TeacherService teacherService,
                                     AmqpTemplate amqpTemplate,
                                     MybatisPlusQueryGenerator<TeacherEntity> queryGenerator) {
        this.teacherService = teacherService;
        this.queryGenerator = queryGenerator;
    }

    @Override
    public boolean isSupport(DepartmentEntity entity) {
        return DepartmentTypeEnum.TEACHER.equals(entity.getType());
    }

    @Override
    public List<BasicUserDetails<Integer>> loadUser(DepartmentEntity entity) {
        List<TeacherEntity> entities = find(entity, null);
        return entities
                .stream()
                .map(e -> BasicUserDetails.of(e.getId(), e.getRealName(), ResourceSourceEnum.TEACHER_SOURCE_VALUE)).
                collect(Collectors.toList());
    }

    @Override
    public void postDelete(DepartmentEntity entity) {
        find(entity, null)
                .stream()
                .peek(t -> t.getDepartmentsInfo().removeIf(d -> entity.getId().equals(d.getId())))
                .forEach(teacherService::save);
    }

    @Override
    public void removeUser(DepartmentEntity entity, List<Integer> userIds) {
        find(entity, userIds)
                .stream()
                .peek(c -> c.getDepartmentsInfo().removeIf(d -> d.getId().equals(entity.getId())))
                .forEach(teacherService::save);
    }

    private List<TeacherEntity> find(DepartmentEntity entity, List<Integer> userIds) {
        QueryWrapper<TeacherEntity> wrapper = createWrapper(entity);

        if (CollectionUtils.isNotEmpty(userIds)) {
            wrapper.in(IdEntity.ID_FIELD_NAME, userIds);
        }

        return teacherService.find(wrapper);
    }

    @Override
    public int countPerson(DepartmentEntity entity) {
        Wrapper<TeacherEntity> wrapper = createWrapper(entity);
        return (int) teacherService.count(wrapper);
    }

    private QueryWrapper<TeacherEntity> createWrapper(DepartmentEntity entity) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        filter.add("filter_[departments_info.id_jin]", entity.getId().toString());

        return queryGenerator.createQueryWrapperFromMap(filter);
    }

}
