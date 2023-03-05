package com.github.dactiv.saas.config.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.ExportDataMeta;
import com.github.dactiv.saas.commons.domain.meta.IdValueMeta;
import com.github.dactiv.saas.commons.domain.meta.ImportDataMeta;
import com.github.dactiv.saas.commons.enumeration.ImportExportTypeEnum;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.config.config.ApplicationConfig;
import com.github.dactiv.saas.config.domain.entity.dictionary.DataDictionaryEntity;
import com.github.dactiv.saas.config.domain.meta.DataDictionaryMeta;
import com.github.dactiv.saas.config.service.EnumerateResourceService;
import com.github.dactiv.saas.config.service.dictionary.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;

import java.util.*;

/**
 * 配置管理控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RefreshScope
@RestController
public class SystemController implements InitializingBean {

    public static final String DEFAULT_EVN_URI = "actuator/env";

    private final DictionaryService dictionaryService;

    private final EnumerateResourceService enumerateResourceService;

    private final ApplicationConfig applicationConfig;

    private final DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate;

    private final RedissonClient redissonClient;

    private final MinioTemplate minioTemplate;

    public SystemController(DictionaryService dictionaryService,
                            EnumerateResourceService enumerateResourceService,
                            DiscoveryClient discoveryClient,
                            ApplicationConfig applicationConfig,
                            RestTemplate restTemplate,
                            RedissonClient redissonClient,
                            MinioTemplate minioTemplate) {

        this.dictionaryService = dictionaryService;
        this.enumerateResourceService = enumerateResourceService;
        this.discoveryClient = discoveryClient;
        this.applicationConfig = applicationConfig;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
        this.minioTemplate = minioTemplate;
    }

    /**
     * 根据名称集合，分组获取所有数据字典
     *
     * @param names 字典名称集合
     * @return 分组数据字典
     */
    @GetMapping("findGroupDataDictionaries")
    public Map<String, List<DataDictionaryMeta>> findGroupDataDictionaries(@RequestParam List<String> names) {
        Map<String, List<DataDictionaryMeta>> group = new LinkedHashMap<>();
        for (String name : names) {
            group.put(name, findDataDictionaries(name));
        }
        return group;
    }

    /**
     * 获取数据字典
     *
     * @param name 字典名称
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{name:.*}")
    public List<DataDictionaryMeta> findDataDictionaries(@PathVariable String name) {

        int index = StringUtils.indexOf(name, "*");

        LambdaQueryWrapper<DataDictionaryEntity> wrapper = Wrappers.lambdaQuery();

        wrapper.select(
                DataDictionaryEntity::getName,
                DataDictionaryEntity::getValue,
                DataDictionaryEntity::getValueType,
                DataDictionaryEntity::getLevel
        );

        if (index > 0) {
            wrapper.like(DataDictionaryEntity::getCode, StringUtils.substring(name, 0, index));
        } else {
            wrapper.eq(DataDictionaryEntity::getCode, name);
        }

        wrapper.orderByAsc(DataDictionaryEntity::getSort);

        return dictionaryService
                .getDataDictionaryService()
                .find(wrapper)
                .stream()
                .map(e -> Casts.of(e, DataDictionaryMeta.class))
                .peek(e -> e.setValue(Casts.cast(e.getValue(), e.getValueType().getClassType())))
                .peek(e -> e.setValueType(null))
                .toList();

    }

    /**
     * 查询数据字典
     *
     * @param map key 为字典简称，value 为要查询的值，格式与 {@link #findDataDictionaries(String)} 一样
     * @return 简称对应的值类型
     */
    @PostMapping("queryDataDictionaries")
    public Map<String, List<DataDictionaryMeta>> queryDataDictionaries(@RequestBody Map<String, String> map) {

        Map<String, List<DataDictionaryMeta>> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(entry.getKey(), findDataDictionaries(entry.getValue()));
        }

        return result;
    }

    /**
     * 获取服务枚举
     *
     * @param service       服务名
     * @param enumerateName 枚举名
     * @return 枚举信息
     */
    @GetMapping("getServiceEnumerate")
    public Map<String, Object> getServiceEnumerate(@RequestParam String service,
                                                   @RequestParam String enumerateName,
                                                   @RequestParam(required = false) List<String> ignoreValue) {
        return enumerateResourceService.getServiceEnumerate(service, enumerateName, ignoreValue);
    }

    /**
     * 批量获取服务枚举
     *
     * @param map key 为 service 值，value 为 enumerateName
     * @return 服务枚举名称 为 key，对应的枚举集合为 value
     */
    @PostMapping("getServiceEnumerates")
    public Map<String, Map<String, Map<String, Object>>> getServiceEnumerates(@RequestBody Map<String, List<IdValueMeta<String, List<String>>>> map) {

        Map<String, Map<String, Map<String, Object>>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<IdValueMeta<String, List<String>>>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Map<String, Object>> valueMap = new LinkedHashMap<>();
            for (IdValueMeta<String, List<String>> value : entry.getValue()) {
                Map<String, Object> enumerate = enumerateResourceService.getServiceEnumerate(key, value.getId(), value.getValue());
                valueMap.put(value.getId(), enumerate);
            }
            result.put(key, valueMap);
        }

        return result;

    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("enumerate")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "系统枚举查询", id = "enumerate", parent = "admin", icon = "icon-file-common", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return enumerateResourceService.getServiceEnumerate();
    }

    /**
     * 同步所有枚举
     *
     * @return 所有服务枚举信息
     */
    @PostMapping("syncEnumerate")
    @Idempotent(key = "config:sync-enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:sync]')")
    @Plugin(name = "同步所有枚举", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() {
        enumerateResourceService.syncEnumerate();
        return RestResult.ofSuccess("同步系统枚举成功", enumerateResourceService.getServiceEnumerate());

    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("environment")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "环境变量查询", id = "environment", parent = "admin", icon = "icon-variable", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Object> environment() {

        Map<String, Object> result = new LinkedHashMap<>();

        List<String> services = discoveryClient.getServices();

        services.forEach(s -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(s);

            ServiceInstance instance = instances.get(RandomUtils.nextInt(0, instances.size()));

            if (Objects.nonNull(instance)) {
                String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;
                try {
                    //noinspection unchecked
                    Map<String, Object> data = restTemplate.getForObject(url, Map.class);
                    result.put(s, data);
                } catch (Exception e) {
                    log.warn("获取 [" + s + "] 服务环境变量出错", e);
                }
            }
        });

        return result;
    }

    /**
     * 获取环境变量值
     *
     * @param service 服务名称
     * @param key     键
     * @return reset 结果集
     */
    @GetMapping("getEnvironment")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getEnvironment(@RequestParam String service, @RequestParam String key) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        Assert.isTrue(CollectionUtils.isNotEmpty(instances), "找不到服务为 [" + service + "] 的实例");

        ServiceInstance instance = instances.get(RandomUtils.nextInt(0, instances.size()));
        Map<String, Object> data = new LinkedHashMap<>();

        if (Objects.nonNull(instance)) {
            String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;
            try {
                //noinspection unchecked
                data = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                log.warn("获取 [" + service + "] 服务环境变量出错", e);
            }
        }

        if (MapUtils.isEmpty(data)) {
            return RestResult.ofSuccess(null);
        }

        for (Map.Entry<String, Object> d : data.entrySet()) {
            //noinspection unchecked
            List<Object> list = Casts.cast(d.getValue(), List.class);
            for (Object o : list) {
                Map<String, Object> map = Casts.cast(o);
                //noinspection unchecked
                Map<String, Object> properties = Casts.cast(map.get(StringLookupFactory.KEY_PROPERTIES), Map.class);
                if (MapUtils.isNotEmpty(properties) && properties.containsKey(key)) {
                    return RestResult.ofSuccess(properties.get(key));
                }
            }
        }

        return RestResult.ofSuccess(null);
    }

    /**
     * 获取环境变量值
     *
     * @param service 服务
     * @param key     健名称
     * @return 值
     */
    @GetMapping("getEnvironmentValue")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getEnvironmentValue(@RequestParam String service, @RequestParam String key) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);

        if (CollectionUtils.isEmpty(instances)) {
            return RestResult.ofSuccess(null);
        }

        ServiceInstance instance = instances.get(RandomUtils.nextInt(0, instances.size()));
        if (Objects.isNull(instance)) {
            return RestResult.ofSuccess(null);
        }

        String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;

        try {
            //noinspection unchecked
            Map<String, List<Map<String, Object>>> data = restTemplate.getForObject(url, Map.class);

            if (MapUtils.isEmpty(data)) {
                return RestResult.ofSuccess(null);
            }

            return RestResult.ofSuccess(getEnvironmentValue(data, key));

        } catch (Exception e) {
            log.warn("获取 [" + service + "] 服务环境变量出错", e);
        }

        return RestResult.ofSuccess(null);
    }

    private List<Map<String, Object>> getHasPropertiesValueEnvironment(List<Map<String, Object>> data) {
        List<Map<String, Object>> result = new LinkedList<>();

        for (Map<String, Object> map : data) {
            if (!map.containsKey("properties")) {
                continue;
            }
            Map<String, Object> properties = new LinkedHashMap<>(map);
            result.add(properties);
        }

        return result;
    }

    private Object getEnvironmentValue(Map<String, List<Map<String, Object>>> data, String key) {

        if (applicationConfig.getIgnoreEnvironmentStartWith().stream().anyMatch(key::startsWith)) {
            return null;
        }

        for (Map.Entry<String, List<Map<String, Object>>> entry : data.entrySet()) {
            List<Map<String, Object>> properties = Casts.cast(entry.getValue());
            List<Map<String, Object>> environmentList = getHasPropertiesValueEnvironment(properties);

            for (Map<String, Object> environment : environmentList) {
                //noinspection unchecked
                Map<String, Object> environmentProperties = Casts.cast(environment.get("properties"), Map.class);

                if (environmentProperties.containsKey(key)) {
                    Map<String, Object> valueMap = Casts.cast(environmentProperties.get(key));
                    return valueMap.get(Property.VALUE_FIELD);
                }
            }
        }

        return null;
    }

    /**
     * 获取环境变量值
     *
     * @param map key 为服务名称，value 为环境变量 key
     * @return 服务名为 key 值为环境变量名和对应的值
     */
    @GetMapping("getEnvironmentValues")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Map<String, Object>> getEnvironmentValues(@RequestBody Map<String, List<String>> map) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        Map<String, Map<String, List<Map<String, Object>>>> cache = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            Map<String, List<Map<String, Object>>> serviceEnvironment = cache.get(entry.getKey());

            if (MapUtils.isEmpty(serviceEnvironment)) {
                List<ServiceInstance> instances = discoveryClient.getInstances(entry.getKey());
                ServiceInstance instance = instances.get(RandomUtils.nextInt(0, instances.size()));
                String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;

                try {
                    //noinspection unchecked
                    Map<String, List<Map<String, Object>>> data = restTemplate.getForObject(url, Map.class);
                    cache.put(entry.getKey(), data);
                } catch (Exception e) {
                    log.warn("获取 [" + entry.getKey() + "] 服务环境变量出错", e);
                }
            }

            if (MapUtils.isEmpty(serviceEnvironment)) {
                continue;
            }

            Map<String, Object> valueMap = new LinkedHashMap<>();

            for (String key : entry.getValue()) {
                Object value = getEnvironmentValue(serviceEnvironment, key);
                if (Objects.isNull(value)) {
                    continue;
                }
                valueMap.put(key, value);
            }

            if (MapUtils.isNotEmpty(valueMap)) {
                result.put(entry.getKey(), valueMap);
            }

        }

        return result;
    }

    /**
     * 获取所有导出的文件内容
     *
     * @return 导出的文件内容
     */
    @GetMapping("exportList")
    @PreAuthorize("hasAuthority('perms[resource_system:export_list]')")
    @Plugin(name = "导出数据查询", id = "export", icon = "icon-export", parent = "resources", type = ResourceType.Menu, sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE, ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE})
    public List<ExportDataMeta> exportList(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        String name = securityUserDetails.getId()
                + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE
                + securityUserDetails.getType()
                + CacheProperties.DEFAULT_SEPARATOR
                + CorsConfiguration.ALL;

        String pattern = applicationConfig.getUserExportCache().getName(name);
        Iterable<String> iterable = redissonClient.getKeys().getKeysByPattern(pattern);

        List<String> keys = new LinkedList<>();
        CollectionUtils.addAll(keys, iterable);

        Map<String, ExportDataMeta> meta = redissonClient.getBuckets().get(keys.toArray(new String[0]));
        List<ExportDataMeta> result = new LinkedList<>(meta.values());
        result.sort(Comparator.comparing(ExportDataMeta::getCreationTime).reversed());

        return result;
    }

    @PostMapping("deleteExport")
    @PreAuthorize("hasAuthority('perms[resource_system:delete_export]')")
    @Plugin(name = "删除导出数据", parent = "resources", sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.MOBILE_MEMBER_SOURCE_VALUE, ResourceSourceEnum.WECHAT_MEMBER_SOURCE_VALUE})
    public RestResult<?> deleteExport(@CurrentSecurityContext SecurityContext securityContext,
                                      @RequestParam List<String> ids) {
        SecurityUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

        redissonClient.getKeys().delete(
                ids
                        .stream()
                        .map(s -> applicationConfig.getUserExportCache().getName(user.getId() + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE + user.getType() + CacheProperties.DEFAULT_SEPARATOR + s)).toArray(String[]::new)
        );
        return RestResult.of("删除 " + ids.size() + "数据成功");
    }

    /**
     * 查找导入数据
     *
     * @param securityContext spring 安全上下文
     * @param type 导入数据类型
     *
     * @return 导入数据集合
     */
    @PostMapping("importList")
    @PreAuthorize("isAuthenticated()")
    public List<ImportDataMeta> importList(@CurrentSecurityContext SecurityContext securityContext, @RequestParam String type) {
        ImportExportTypeEnum typeEnum = ValueEnumUtils.parse(type, ImportExportTypeEnum.class);
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        String name = userDetails.getId()
                + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE
                + userDetails.getType()
                + CacheProperties.DEFAULT_SEPARATOR
                + typeEnum.getValue()
                + CacheProperties.DEFAULT_SEPARATOR
                + CorsConfiguration.ALL;

        String key = applicationConfig.getUserImportCache().getName(name);
        Iterable<String> iterable = redissonClient.getKeys().getKeysByPattern(key);
        List<String> keys = new LinkedList<>();
        CollectionUtils.addAll(keys, iterable);

        Map<String, ImportDataMeta> meta = redissonClient.getBuckets().get(keys.toArray(new String[0]));
        List<ImportDataMeta> result = new LinkedList<>(meta.values());
        result.sort(Comparator.comparing(ImportDataMeta::getCreationTime).reversed());

        return result;
    }

    /**
     * 获取导入数据信息
     *
     * @param securityContext spring 安全上下文
     * @param type 导入类型
     * @param id 主键 id
     *
     * @return 导入数据元数据
     */
    @GetMapping("getImport")
    @PreAuthorize("isAuthenticated()")
    public ImportDataMeta getImport(@CurrentSecurityContext SecurityContext securityContext,
                                    @RequestParam String type,
                                    @RequestParam String id) {

        ImportExportTypeEnum typeEnum = ValueEnumUtils.parse(type, ImportExportTypeEnum.class);
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        String name = userDetails.getId()
                + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE
                + userDetails.getType()
                + CacheProperties.DEFAULT_SEPARATOR
                + typeEnum.getValue()
                + CacheProperties.DEFAULT_SEPARATOR
                + id;

        String key = applicationConfig.getUserImportCache().getName(name);
        RBucket<ImportDataMeta> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        minioTemplate.makeBucketIfNotExists(SystemConstants.EXPORT_BUCKET);
    }

}
