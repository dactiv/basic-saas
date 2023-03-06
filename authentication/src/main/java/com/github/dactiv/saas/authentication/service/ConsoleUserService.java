package com.github.dactiv.saas.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.authentication.consumer.CountDepartmentPersonConsumer;
import com.github.dactiv.saas.authentication.dao.ConsoleUserDao;
import com.github.dactiv.saas.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.commons.ErrorCodeConstants;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * tb_console_user 的业务逻辑
 *
 * <p>Table: tb_console_user - 后台用户表</p>
 *
 * @author maurice.chen
 * @see ConsoleUserEntity
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsoleUserService extends BasicService<ConsoleUserDao, ConsoleUserEntity> {

    private final AuthorizationService authorizationService;

    private final AmqpTemplate amqpTemplate;

    public ConsoleUserService(AuthorizationService authorizationService,
                              AmqpTemplate amqpTemplate) {
        this.authorizationService = authorizationService;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {

        Wrapper<ConsoleUserEntity> wrapper = Wrappers
                .<ConsoleUserEntity>lambdaQuery()
                .select(ConsoleUserEntity::getUsername, SystemUserEntity::getEmail, ConsoleUserEntity::getPhoneNumber)
                .in(ConsoleUserEntity::getId, ids);

        List<ConsoleUserEntity> users = find(wrapper);

        users.forEach(authorizationService::deleteSystemUserAuthenticationCache);
        users.forEach(authorizationService::expireSystemUserSession);

        return super.deleteById(ids, errorThrow);
    }

    @Override
    public int save(ConsoleUserEntity entity) {
        boolean isNew = Objects.isNull(entity.getId());

        List<Integer> syncDepartments = new LinkedList<>();

        if (isNew) {
            boolean usernameExist = lambdaQuery()
                    .select(ConsoleUserEntity::getId)
                    .eq(ConsoleUserEntity::getUsername, entity.getUsername())
                    .exists();

            if (usernameExist) {
                throw new ErrorCodeException("登陆账户 [" + entity.getUsername() + "] 已存在", ErrorCodeConstants.CONTENT_EXIST);
            }

            boolean emailExist = lambdaQuery()
                    .select(ConsoleUserEntity::getId)
                    .eq(ConsoleUserEntity::getEmail, entity.getEmail())
                    .exists();

            if (emailExist) {
                throw new ErrorCodeException("邮箱账户 [" + entity.getEmail() + "] 已存在", ErrorCodeConstants.CONTENT_EXIST);
            }

            PasswordEncoder passwordEncoder = authorizationService
                    .getUserDetailsService(ResourceSourceEnum.CONSOLE)
                    .getPasswordEncoder();

            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
            syncDepartments.addAll(entity.getDepartmentsMetas().stream().map(IdEntity::getId).toList());
        } else {
            ConsoleUserEntity ormEntity = Objects.requireNonNull(get(entity.getId()), "找不到 ID 为 [" + entity.getId() + "] 的后台用户信息");
            if (CollectionUtils.isNotEmpty(ormEntity.getDepartmentsMetas())) {
                ormEntity
                        .getDepartmentsMetas()
                        .stream()
                        .filter(s -> entity.getDepartmentsMetas().stream().noneMatch(n -> n.getId().equals(s.getId())))
                        .map(IdEntity::getId)
                        .forEach(syncDepartments::add);
            } else if (CollectionUtils.isNotEmpty(entity.getDepartmentsMetas())) {
                syncDepartments.addAll(entity.getDepartmentsMetas().stream().map(IdEntity::getId).toList());
            }
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

    /**
     * 通过登录账号或邮箱获取后台用户
     *
     * @param identity 登录账号或邮箱
     * @return 后台用户
     */
    public ConsoleUserEntity getByIdentity(String identity) {
        return lambdaQuery()
                .eq(ConsoleUserEntity::getUsername, identity)
                .or()
                .eq(ConsoleUserEntity::getEmail, identity)
                .one();
    }

    public void adminUpdatePassword(Integer id, String newPassword) {
        ConsoleUserEntity entity = get(id);
        ConsoleUserEntity update = entity.ofIdData();

        String password = authorizationService
                .getUserDetailsService(ResourceSourceEnum.CONSOLE)
                .getPasswordEncoder()
                .encode(newPassword);

        update.setPassword(password);
        updateById(update);

        authorizationService.expireSystemUserSession(entity);
    }
}
