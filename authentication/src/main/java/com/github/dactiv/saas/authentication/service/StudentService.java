package com.github.dactiv.saas.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.authentication.consumer.CountDepartmentPersonConsumer;
import com.github.dactiv.saas.authentication.dao.StudentDao;
import com.github.dactiv.saas.authentication.domain.entity.StudentEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.feign.AdminServiceFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_student 的业务逻辑
 *
 * <p>Table: tb_student - 学生表</p>
 *
 * @author maurice.chen
 * @see StudentEntity
 * @since 2022-05-28 01:03:16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StudentService extends BasicService<StudentDao, StudentEntity> implements WechatAuthenticationService<StudentEntity> {

    private final AdminServiceFeignClient adminServiceFeignClient;

    private final AuthorizationService authorizationService;

    private final AmqpTemplate amqpTemplate;

    public StudentService(AdminServiceFeignClient adminServiceFeignClient,
                          AmqpTemplate amqpTemplate,
                          AuthorizationService authorizationService) {

        this.amqpTemplate = amqpTemplate;
        this.authorizationService = authorizationService;
        this.adminServiceFeignClient = adminServiceFeignClient;
    }

    public StudentEntity getByUsername(String username) {
        return lambdaQuery()
                .eq(SystemUserEntity::getUsername, username)
                .or()
                .eq(StudentEntity::getPhoneNumber, username)
                .or()
                .eq(SystemUserEntity::getEmail, username)
                .or()
                .eq(StudentEntity::getNumber, username)
                .one();
    }

    @Override
    public int save(StudentEntity entity) {
        boolean isNew = Objects.isNull(entity.getId());

        List<Integer> syncDepartments = new LinkedList<>();

        if (!isNew) {
            StudentEntity ormEntity = Objects.requireNonNull(get(entity.getId()), "找不到 ID 为 [" + entity.getId() + "] 的学生信息");
            if (CollectionUtils.isNotEmpty(ormEntity.getDepartmentsInfo())) {
                ormEntity
                        .getDepartmentsInfo()
                        .stream()
                        .filter(s -> entity.getDepartmentsInfo().stream().noneMatch(n -> n.getId().equals(s.getId())))
                        .map(IdEntity::getId)
                        .forEach(syncDepartments::add);
            } else if (CollectionUtils.isNotEmpty(entity.getDepartmentsInfo())) {
                syncDepartments.addAll(entity.getDepartmentsInfo().stream().map(IdEntity::getId).toList());
            }
        } else {
            String password = authorizationService
                    .getUserDetailsService(ResourceSourceEnum.STUDENT)
                    .getPasswordEncoder()
                    .encode(entity.getPassword());

            entity.setPassword(password);

            if (lambdaQuery().eq(StudentEntity::getNumber, entity.getNumber()).exists()) {
                throw new ServiceException("学生学号 [" + entity.getNumber() + "] 已存在");
            }

            syncDepartments.addAll(entity.getDepartmentsInfo().stream().map(IdEntity::getId).toList());
        }

        if (CollectionUtils.isNotEmpty(syncDepartments)) {
            syncDepartments.forEach(syncId ->
                    amqpTemplate.convertAndSend(
                            SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE,
                            CountDepartmentPersonConsumer.DEFAULT_QUEUE_NAME,
                            syncId,
                            message -> {
                                String id = CountDepartmentPersonConsumer.DEFAULT_QUEUE_NAME + Casts.DEFAULT_DOT_SYMBOL + syncId;
                                message.getMessageProperties().setMessageId(id);
                                message.getMessageProperties().setCorrelationId(syncId.toString());
                                return message;
                            }
                    )
            );
        }

        return super.save(entity);
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        this.adminServiceFeignClient.deleteUserDetails(ids.stream().map(id -> Casts.cast(id, Integer.class)).collect(Collectors.toList()));

        Wrapper<StudentEntity> wrapper = Wrappers
                .<StudentEntity>lambdaQuery()
                .select(StudentEntity::getUsername, StudentEntity::getEmail, StudentEntity::getPhoneNumber)
                .in(StudentEntity::getId, ids);

        List<StudentEntity> users = find(wrapper);

        users.forEach(authorizationService::deleteSystemUserAuthenticationCache);
        users.forEach(authorizationService::expireSystemUserSession);

        return super.deleteById(ids, errorThrow);
    }

    public void adminUpdatePassword(Integer id, String newPassword) {
        StudentEntity entity = get(id);
        StudentEntity update = entity.ofIdData();

        String password = authorizationService
                .getUserDetailsService(ResourceSourceEnum.STUDENT)
                .getPasswordEncoder()
                .encode(newPassword);

        update.setPassword(password);
        updateById(update);

        authorizationService.expireSystemUserSession(entity);
    }

    @Override
    public StudentEntity getByPhoneNumber(String phoneNumber) {
        return getByUsername(phoneNumber);
    }

    public StudentEntity getByWechatOpenId(String openId) {
        return lambdaQuery().eq(StudentEntity::getOpenId, openId).one();
    }
}
