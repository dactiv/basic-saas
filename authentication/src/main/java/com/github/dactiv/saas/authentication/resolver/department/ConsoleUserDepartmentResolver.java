package com.github.dactiv.saas.authentication.resolver.department;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.saas.authentication.enumerate.DepartmentTypeEnum;
import com.github.dactiv.saas.authentication.resolver.DepartmentResolver;
import com.github.dactiv.saas.authentication.service.ConsoleUserService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * 后台用户解析器实现
 *
 * @author maurice.chen
 */
@Component
public class ConsoleUserDepartmentResolver implements DepartmentResolver {

    private final ConsoleUserService consoleUserService;
    private final MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator;

    public ConsoleUserDepartmentResolver(ConsoleUserService consoleUserService,
                                         MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator) {
        this.consoleUserService = consoleUserService;
        this.queryGenerator = queryGenerator;
    }

    @Override
    public boolean isSupport(DepartmentEntity entity) {
        return DepartmentTypeEnum.CONSOLE_USER.equals(entity.getType());
    }

    @Override
    public List<BasicUserDetails<Integer>> loadUser(DepartmentEntity entity) {
        List<ConsoleUserEntity> entities = find(entity, null);
        return entities
                .stream()
                .map(e -> BasicUserDetails.of(e.getId(), e.getRealName(), ResourceSourceEnum.CONSOLE_SOURCE_VALUE))
                .toList();
    }

    @Override
    public void postDelete(DepartmentEntity entity) {
        find(entity, null)
                .stream()
                .peek(t -> t.getDepartmentsMetas().removeIf(d -> entity.getId().equals(d.getId())))
                .forEach(consoleUserService::save);
    }

    @Override
    public void removeUser(DepartmentEntity entity, List<Integer> userIds) {
        find(entity, userIds)
                .stream()
                .peek(c -> c.getDepartmentsMetas().removeIf(d -> d.getId().equals(entity.getId())))
                .forEach(consoleUserService::save);
    }

    private List<ConsoleUserEntity> find(DepartmentEntity entity, List<Integer> userIds) {
        QueryWrapper<ConsoleUserEntity> wrapper = createWrapper(entity);

        if (CollectionUtils.isNotEmpty(userIds)) {
            wrapper.in(IdEntity.ID_FIELD_NAME, userIds);
        }

        return consoleUserService.find(wrapper);
    }

    @Override
    public int countPerson(DepartmentEntity entity) {
        Wrapper<ConsoleUserEntity> wrapper = createWrapper(entity);
        return (int) consoleUserService.count(wrapper);
    }

    private QueryWrapper<ConsoleUserEntity> createWrapper(DepartmentEntity entity) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        filter.add("filter_[departments_info.id_jin]", entity.getId().toString());

        return queryGenerator.createQueryWrapperFromMap(filter);
    }

}
