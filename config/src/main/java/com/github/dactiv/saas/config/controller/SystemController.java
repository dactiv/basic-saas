package com.github.dactiv.saas.config.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.query.Property;
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
 * ?????????????????????
 *
 * @author maurice.chen
 */
@Slf4j
@RefreshScope
@RestController
public class SystemController {

    public static final String DEFAULT_EVN_URI = "actuator/env";

    private final DictionaryService dictionaryService;

    private final EnumerateResourceService enumerateResourceService;

    private final ApplicationConfig applicationConfig;

    private final DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate;

    private final RedissonClient redissonClient;

    public SystemController(DictionaryService dictionaryService,
                            EnumerateResourceService enumerateResourceService,
                            DiscoveryClient discoveryClient,
                            ApplicationConfig applicationConfig,
                            RestTemplate restTemplate,
                            RedissonClient redissonClient) {

        this.dictionaryService = dictionaryService;
        this.enumerateResourceService = enumerateResourceService;
        this.discoveryClient = discoveryClient;
        this.applicationConfig = applicationConfig;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param names ??????????????????
     * @return ??????????????????
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
     * ??????????????????
     *
     * @param name ????????????
     * @return ??????????????????
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
     * ??????????????????
     *
     * @param map key ??????????????????value ?????????????????????????????? {@link #findDataDictionaries(String)} ??????
     * @return ????????????????????????
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
     * ??????????????????
     *
     * @param service       ?????????
     * @param enumerateName ?????????
     * @return ????????????
     */
    @GetMapping("getServiceEnumerate")
    public Map<String, Object> getServiceEnumerate(@RequestParam String service,
                                                   @RequestParam String enumerateName,
                                                   @RequestParam(required = false) List<String> ignoreValue) {
        return enumerateResourceService.getServiceEnumerate(service, enumerateName, ignoreValue);
    }

    /**
     * ????????????????????????
     *
     * @param map key ??? service ??????value ??? enumerateName
     * @return ?????????????????? ??? key??????????????????????????? value
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
     * ??????????????????
     *
     * @return ????????????????????????
     */
    @PostMapping("syncEnumerate")
    @Idempotent(key = "config:sync-enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:sync]')")
    @Plugin(name = "??????????????????", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() {
        enumerateResourceService.syncEnumerate();
        return RestResult.ofSuccess("????????????????????????", enumerateResourceService.getServiceEnumerate());

    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
     */
    @GetMapping("enumerate")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "??????????????????", id = "enumerate", parent = "basic", icon = "icon-file-common", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return enumerateResourceService.getServiceEnumerate();
    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
     */
    @GetMapping("environment")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "??????????????????", id = "environment", parent = "basic", icon = "icon-variable", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
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
                    log.warn("?????? [" + s + "] ????????????????????????", e);
                }
            }
        });

        return result;
    }

    /**
     * ?????????????????????
     *
     * @param service ????????????
     * @param key     ???
     * @return reset ?????????
     */
    @GetMapping("getEnvironment")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getEnvironment(@RequestParam String service, @RequestParam String key) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        Assert.isTrue(CollectionUtils.isNotEmpty(instances), "?????????????????? [" + service + "] ?????????");

        ServiceInstance instance = instances.get(RandomUtils.nextInt(0, instances.size()));
        Map<String, Object> data = new LinkedHashMap<>();

        if (Objects.nonNull(instance)) {
            String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;
            try {
                //noinspection unchecked
                data = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                log.warn("?????? [" + service + "] ????????????????????????", e);
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
     * ?????????????????????
     *
     * @param service ??????
     * @param key     ?????????
     * @return ???
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
            log.warn("?????? [" + service + "] ????????????????????????", e);
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
     * ?????????????????????
     *
     * @param map key ??????????????????value ??????????????? key
     * @return ???????????? key ????????????????????????????????????
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
                    log.warn("?????? [" + entry.getKey() + "] ????????????????????????", e);
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
     * ?????????????????????????????????
     *
     * @return ?????????????????????
     */
    @GetMapping("export")
    @PreAuthorize("hasAuthority('perms[resource:export]')")
    @Plugin(name = "??????????????????", id = "export", icon = "icon-export", parent = "resource", type = ResourceType.Menu, sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.MEMBER_SOURCE_VALUE})
    public List<ExportDataMeta> export(@CurrentSecurityContext SecurityContext securityContext) {
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
    @PreAuthorize("hasAuthority('perms[resource:delete_export]')")
    @Plugin(name = "??????????????????", parent = "resource", sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.MEMBER_SOURCE_VALUE})
    public RestResult<?> deleteExport(@CurrentSecurityContext SecurityContext securityContext,
                                      @RequestParam List<String> ids) {
        SecurityUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

        redissonClient.getKeys().delete(
                ids
                        .stream()
                        .map(s -> applicationConfig.getUserExportCache().getName(user.getId() + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE + user.getType() + CacheProperties.DEFAULT_SEPARATOR + s)).toArray(String[]::new)
        );
        return RestResult.of("?????? " + ids.size() + "????????????");
    }

    /**
     * ??????????????????
     *
     * @param securityContext spring ???????????????
     * @param type ??????????????????
     *
     * @return ??????????????????
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
     * ????????????????????????
     *
     * @param securityContext spring ???????????????
     * @param type ????????????
     * @param id ?????? id
     *
     * @return ?????????????????????
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

}
