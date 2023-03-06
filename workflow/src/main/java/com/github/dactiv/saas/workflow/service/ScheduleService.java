package com.github.dactiv.saas.workflow.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.workflow.config.ApplicationConfig;
import com.github.dactiv.saas.workflow.dao.ScheduleDao;
import com.github.dactiv.saas.workflow.domain.body.ScheduleBody;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleEntity;
import com.github.dactiv.saas.workflow.domain.entity.ScheduleParticipantEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.*;

/**
 * tb_schedule 的业务逻辑
 *
 * <p>Table: tb_schedule - 日程表</p>
 *
 * @author maurice.chen
 * @see ScheduleEntity
 * @since 2022-03-03 02:31:54
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService extends BasicService<ScheduleDao, ScheduleEntity> {

    public static final String DEFAULT_URL = "https://jiejiari.market.alicloudapi.com/holidayList";

    @Getter
    private final ScheduleParticipantService scheduleParticipantService;

    private final ApplicationConfig applicationConfig;

    private final RestTemplate restTemplate;

    private final RedissonClient redissonClient;

    public ScheduleService(ScheduleParticipantService scheduleParticipantService,
                           ApplicationConfig applicationConfig,
                           RestTemplate restTemplate,
                           RedissonClient redissonClient) {
        this.scheduleParticipantService = scheduleParticipantService;
        this.applicationConfig = applicationConfig;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
    }

    public int save(ScheduleEntity entity, Boolean publish) {

        if (Objects.nonNull(entity.getId())) {
            entity.setStatus(DataRecordStatusEnum.UPDATE);
        }

        int result = super.save(entity);

        if (ScheduleBody.class.isAssignableFrom(entity.getClass())) {
            ScheduleBody body = Casts.cast(entity);

            List<ScheduleParticipantEntity> participantList = scheduleParticipantService.findByScheduleId(entity.getId());
            List<Integer> currentIds = body
                    .getParticipantList()
                    .stream()
                    .map(ScheduleParticipantEntity::getId)
                    .filter(Objects::nonNull)
                    .toList();

            List<Integer> deleteIds = participantList
                    .stream()
                    .map(ScheduleParticipantEntity::getId)
                    .filter(id -> !currentIds.contains(id))
                    .toList();

            if (CollectionUtils.isNotEmpty(deleteIds)) {
                scheduleParticipantService.deleteById(deleteIds);
            }

            body.getParticipantList()
                    .stream()
                    .peek(p -> p.setScheduleId(entity.getId()))
                    .forEach(scheduleParticipantService::save);
        }

        if (publish) {
            publish(entity, null);
        }


        return result;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        List<ScheduleEntity> list = get(ids);
        int sum = list.stream().mapToInt(this::deleteByEntity).sum();
        if (sum != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int deleteById(Serializable id) {
        return super.deleteById(get(id));
    }

    @Override
    public int deleteByEntity(Collection<ScheduleEntity> entities, boolean errorThrow) {
        int sum = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (sum != entities.size() && errorThrow) {
            String msg = "删除 [" + entities + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return sum;
    }

    @Override
    public int deleteByEntity(ScheduleEntity entity) {
        int result =  super.deleteByEntity(entity);
        scheduleParticipantService.deleteByScheduleId(entity.getId());
        return result;
    }

    public ScheduleBody convertScheduleBody(ScheduleEntity entity) {
        ScheduleBody body = Casts.of(entity, ScheduleBody.class);
        body.setParticipantList(scheduleParticipantService.findByScheduleId(body.getId()));
        return body;
    }

    public void publish(List<Integer> ids, SecurityUserDetails userDetails) {
        ids.forEach(id -> publish(get(id), userDetails));
    }

    public void publish(ScheduleEntity entity, SecurityUserDetails userDetails) {
        if (Objects.nonNull(userDetails)) {
            SecurityUserDetailsConstants.contains(List.of(entity), userDetails);
        }
        entity.setStatus(DataRecordStatusEnum.PUBLISH);
        entity.setPublishTime(new Date());

        updateById(entity);
    }

    public RestResult<List<Map<String, Object>>> getHoliday(Integer year) {
        String key = applicationConfig.getHolidayCache().getName(year.toString());
        RBucket<RestResult<List<Map<String, Object>>>> bucket = redissonClient.getBucket(key);

        if (bucket.isExists()) {
            return bucket.get();
        }
        RestResult<List<Map<String, Object>>> result = RestResult.ofSuccess(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "APPCODE " + applicationConfig.getAliYunHolidayAppCode());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(DEFAULT_URL + "?year=" + year, HttpMethod.GET, httpEntity, String.class);
            //noinspection unchecked
            Map<String, Object> body = Casts.readValue(response.getBody(), Map.class);
            //noinspection unchecked
            Map<String, Object> map = Casts.cast(body.get("showapi_res_body"), Map.class);
            if (map.containsKey("data")) {
                result = RestResult.ofSuccess(Casts.cast(map.get("data")));
                TimeProperties timeProperties = applicationConfig.getHolidayCache().getExpiresTime();
                if (Objects.nonNull(timeProperties)) {
                    bucket.setAsync(result, timeProperties.getValue(), timeProperties.getUnit());
                } else {
                    bucket.setAsync(result);
                }
            }

        } catch (Exception e) {
            log.warn("获取 [" + year + "] 的节假日列表出错", e);
        }
        return result;
    }
}
