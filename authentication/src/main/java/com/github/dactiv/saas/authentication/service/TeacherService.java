package com.github.dactiv.saas.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.framework.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.authentication.consumer.CountDepartmentPersonConsumer;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.enumerate.InviteTargetEnum;
import com.github.dactiv.saas.authentication.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.saas.authentication.dao.TeacherDao;
import com.github.dactiv.saas.authentication.domain.entity.TeacherEntity;
import com.github.dactiv.saas.authentication.domain.meta.ClassGradesMeta;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.config.SchoolProperties;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.domain.meta.TeacherClassGradesMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.commons.enumeration.TeacherTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_teacher 的业务逻辑
 *
 * <p>Table: tb_teacher - 教师表</p>
 *
 * @author maurice.chen
 * @see TeacherEntity
 * @since 2022-03-07 11:19:27
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TeacherService extends BasicService<TeacherDao, TeacherEntity> implements WechatAuthenticationService<TeacherEntity> {

    private final SchoolProperties schoolProperties;

    private final AmqpTemplate amqpTemplate;

    private final CipherAlgorithmService cipherService = new CipherAlgorithmService();

    private final AuthorizationService authorizationService;

    private final MybatisPlusQueryGenerator<TeacherEntity> queryGenerator;

    public TeacherService(SchoolProperties schoolProperties,
                          AuthorizationService authorizationService,
                          AmqpTemplate amqpTemplate,
                          MybatisPlusQueryGenerator<TeacherEntity> queryGenerator) {
        this.schoolProperties = schoolProperties;
        this.authorizationService = authorizationService;
        this.amqpTemplate = amqpTemplate;
        this.queryGenerator = queryGenerator;
    }

    @Override
    public int save(TeacherEntity entity) {
        boolean isNew = Objects.isNull(entity.getId());

        List<Integer> syncDepartments = new LinkedList<>();

        if (!isNew) {
            TeacherEntity ormEntity = Objects.requireNonNull(get(entity.getId()), "找不到 ID 为 [" + entity.getId() + "] 的教师信息");
            if (CollectionUtils.isNotEmpty(ormEntity.getDepartmentsInfo())) {
                ormEntity
                        .getDepartmentsInfo()
                        .stream()
                        .filter(s -> entity.getDepartmentsInfo().stream().noneMatch(n -> n.getId().equals(s.getId())))
                        .map(IdEntity::getId)
                        .forEach(syncDepartments::add);
            } else if (CollectionUtils.isNotEmpty(entity.getDepartmentsInfo())) {
                syncDepartments.addAll(entity.getDepartmentsInfo().stream().map(IdEntity::getId).collect(Collectors.toList()));
            }
        } else {
            String password = authorizationService
                    .getUserDetailsService(ResourceSourceEnum.STUDENT)
                    .getPasswordEncoder()
                    .encode(entity.getPassword());

            entity.setPassword(password);

            if (lambdaQuery().eq(TeacherEntity::getNumber, entity.getNumber()).exists()) {
                throw new ServiceException("教师工号 [" + entity.getNumber() + "] 已存在");
            }

            syncDepartments.addAll(entity.getDepartmentsInfo().stream().map(IdEntity::getId).collect(Collectors.toList()));
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
     * 通过登陆账户获取教师信息
     *
     * @param username 登陆账户
     * @return 教师实体
     */
    public TeacherEntity getByUsername(String username) {
        return lambdaQuery()
                .eq(SystemUserEntity::getUsername, username)
                .or()
                .eq(SystemUserEntity::getEmail, username)
                .or()
                .eq(TeacherEntity::getPhoneNumber, username)
                .or()
                .eq(TeacherEntity::getNumber, username)
                .one();
    }

    /**
     * 同步数据
     *
     * @param cipherText 外部密文数据
     * @return 新的密文内容
     */
    public String syncData(String cipherText) {

        RsaCipherService rsaCipherService = cipherService.getCipherService(CipherAlgorithmService.RSA_ALGORITHM);

        ByteSource source = rsaCipherService.decrypt(Base64.decode(cipherText), Base64.decode(schoolProperties.getSecretKey()));
        TeacherEntity entity = Casts.readValue(source.obtainBytes(), TeacherEntity.class);

        syncData(entity);

        return encryptData(entity);
    }

    /**
     * 加密数据
     *
     * @param entity 教师实体
     * @return 加密内容
     */
    public String encryptData(TeacherEntity entity) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add(IdEntity.ID_FIELD_NAME, entity.getId().toString());
        data.add(JsonLogoutSuccessHandler.DEFAULT_TOKEN_NAME, entity.getAccessToken());

        AesCipherService aesCipherService = cipherService.getCipherService(CipherAlgorithmService.AES_ALGORITHM);
        String result = Casts.castRequestBodyMapToString(data);
        ByteSource byteSource = aesCipherService.encrypt(result.getBytes(StandardCharsets.UTF_8), Base64.decode(schoolProperties.getAccessKey()));

        return byteSource.getBase64();
    }

    /**
     * 同步数据结局
     *
     * @param entity 外部提交的教师数据实体
     */
    public void syncData(TeacherEntity entity) {
        TeacherEntity orm = lambdaQuery().eq(TeacherEntity::getTargetId, entity.getId().toString()).one();

        if (Objects.nonNull(orm)) {
            for (String fieldName : TeacherEntity.copyProperties) {
                Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
                if (Objects.isNull(field)) {
                    continue;
                }
                field.setAccessible(true);
                Object value = ReflectionUtils.getField(field, entity);
                ReflectionUtils.setField(field, orm, value);
            }

            String token = InviteTargetEnum.TEACHER + CacheProperties.DEFAULT_SEPARATOR + entity.getId() + System.currentTimeMillis();
            orm.setAccessToken(DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8)));
            save(orm);

        } else {
            entity.setTargetId(entity.getId().toString());
            entity.setId(null);
            String token = InviteTargetEnum.TEACHER + CacheProperties.DEFAULT_SEPARATOR + entity.getTargetId() + System.currentTimeMillis();
            entity.setAccessToken(DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8)));

            save(entity);

        }
    }

    /**
     * 根据科目获取教师集合
     *
     * @param subject 科目值
     * @return 教师集合
     */
    public List<TeacherEntity> findBySubject(Integer subject, List<Integer> ignore) {
        MultiValueMap<String, Object> query = new LinkedMultiValueMap<>();
        query.add("filter_[subjects_info.id_jin]", subject.toString());
        if (CollectionUtils.isNotEmpty(ignore)) {
            query.put("filter_[id_nin]", new LinkedList<>(ignore));
        }
        return find(queryGenerator.createQueryWrapperFromMap(query));
    }

    /**
     * 根据部门获取教师集合
     *
     * @param departmentId 部门 id
     * @return 教师集合
     */
    public List<TeacherEntity> findByDepartment(Integer departmentId, List<Integer> ignore) {
        MultiValueMap<String, Object> query = new LinkedMultiValueMap<>();
        query.add("filter_[departments_info.id_jin]", departmentId.toString());
        if (CollectionUtils.isNotEmpty(ignore)) {
            query.put("filter_[id_nin]", new LinkedList<>(ignore));
        }
        return find(queryGenerator.createQueryWrapperFromMap(query));
    }

    /**
     * 更新教师班级信息
     *
     * @param meta 基础班级信息元数据
     */
    public void updateClassGradesInfo(TeacherClassGradesMeta meta) {
        MultiValueMap<String, Object> query = new LinkedMultiValueMap<>();
        query.add("filter_[class_grades_info.id_jin]", meta.getId().toString());

        List<TeacherEntity> list = find(queryGenerator.createQueryWrapperFromMap(query));

        for (TeacherEntity entity : list) {

            if (entity.getId().equals(meta.getClassTeacherId())) {
                entity.getClassGradesInfo().removeIf(t -> t.getId().equals(meta.getId()) && TeacherTypeEnum.CLASS_TEACHER.equals(t.getType()));
            }

            if (CollectionUtils.isNotEmpty(meta.getTeacherInfo())) {
                for (IdNameMeta idNameMeta : meta.getTeacherInfo()) {
                    if (entity.getId().equals(idNameMeta.getId())) {
                        entity.getClassGradesInfo().removeIf(t -> t.getId().equals(meta.getId()) && TeacherTypeEnum.TEACHER.equals(t.getType()));
                    }
                }
            }

            updateById(entity);
        }

        if (Objects.nonNull(meta.getClassTeacherId())) {
            TeacherEntity entity = get(meta.getClassTeacherId());
            ClassGradesMeta newData = Casts.of(meta, ClassGradesMeta.class);
            addClassGradesInfo(newData, entity, TeacherTypeEnum.CLASS_TEACHER);
        }

        if (CollectionUtils.isNotEmpty(meta.getTeacherInfo())) {
            for (IdNameMeta idNameMeta : meta.getTeacherInfo()) {
                TeacherEntity entity = get(idNameMeta.getId());
                ClassGradesMeta newData = Casts.of(meta, ClassGradesMeta.class);
                addClassGradesInfo(newData, entity, TeacherTypeEnum.TEACHER);
            }
        }
    }

    private void addClassGradesInfo(ClassGradesMeta meta, TeacherEntity entity, TeacherTypeEnum type) {
        List<ClassGradesMeta> classGradesInfo = entity.getClassGradesInfo();
        if (Objects.isNull(classGradesInfo)) {
            classGradesInfo = new LinkedList<>();
        }

        ClassGradesMeta newData = Casts.of(meta, ClassGradesMeta.class);
        newData.setType(type);
        classGradesInfo.add(newData);
        updateById(entity);
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {

        Wrapper<TeacherEntity> wrapper = Wrappers
                .<TeacherEntity>lambdaQuery()
                .select(TeacherEntity::getUsername, TeacherEntity::getEmail, TeacherEntity::getPhoneNumber)
                .in(TeacherEntity::getId, ids);

        List<TeacherEntity> users = find(wrapper);

        users.forEach(authorizationService::deleteSystemUserAuthenticationCache);
        users.forEach(authorizationService::expireSystemUserSession);

        return super.deleteById(ids, errorThrow);
    }

    public void adminUpdatePassword(Integer id, String newPassword) {
        TeacherEntity entity = get(id);
        TeacherEntity update = entity.ofIdData();

        String password = authorizationService
                .getUserDetailsService(ResourceSourceEnum.TEACHER)
                .getPasswordEncoder()
                .encode(newPassword);

        update.setPassword(password);
        updateById(update);

        authorizationService.expireSystemUserSession(entity);
    }

    @Override
    public TeacherEntity getByPhoneNumber(String phoneNumber) {
        return getByUsername(phoneNumber);
    }

    public SystemUserEntity getByNumber(String number) {
        return lambdaQuery().eq(TeacherEntity::getNumber, number).one();
    }

    public TeacherEntity getByWechatOpenId(String openId) {
        return lambdaQuery().eq(TeacherEntity::getOpenId, openId).one();
    }
}
