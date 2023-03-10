package com.github.dactiv.saas.authentication.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.event.NacosInstancesChangeEvent;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceSubscribeEvent;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.PluginInfo;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.saas.authentication.service.AuthorizationService;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ??????????????????
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class PluginResourceService {

    /**
     * ????????????????????????????????? uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    private final RestTemplate restTemplate;

    private final NacosSpringEventManager nacosSpringEventManager;

    private final AuthorizationService authorizationService;

    private final List<PluginResourceInterceptor> pluginResourceInterceptor;

    private final PluginServiceValidator pluginServiceValidator;

    @Getter
    private final ApplicationConfig applicationConfig;

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    private final Map<String, List<PluginInstance>> instanceCache = new LinkedHashMap<>();

    /**
     * ?????????????????????????????????
     */
    private final List<ResourceMeta> resources = new LinkedList<>();

    public PluginResourceService(RestTemplate restTemplate,
                                 NacosSpringEventManager nacosSpringEventManager,
                                 AuthorizationService authorizationService,
                                 PluginServiceValidator pluginServiceValidator,
                                 ApplicationConfig applicationConfig,
                                 List<PluginResourceInterceptor> pluginResourceInterceptor) {
        this.restTemplate = restTemplate;
        this.nacosSpringEventManager = nacosSpringEventManager;
        this.authorizationService = authorizationService;
        this.pluginServiceValidator = pluginServiceValidator;
        this.applicationConfig = applicationConfig;
        this.pluginResourceInterceptor = pluginResourceInterceptor;
    }

    /**
     * ???????????? info
     *
     * @param instance ??????
     * @return ????????????
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInstanceInfo(Instance instance) {

        String http = StringUtils.prependIfMissing(instance.toInetAddr(), SpringMvcUtils.HTTP_PROTOCOL_PREFIX);
        String url = StringUtils.appendIfMissing(http, DEFAULT_PLUGIN_INFO_URL);

        return restTemplate.getForObject(url, Map.class);
    }

    /**
     * ????????????????????????
     *
     * @param target ????????????
     * @param source ?????????
     * @return 0 ???????????????0 ???????????????0 ??????
     */
    public int comparingInstanceVersion(Instance target, Instance source) {
        return getInstanceVersion(target).compareTo(getInstanceVersion(source));
    }

    /**
     * ???????????????????????????
     *
     * @param instance ??????
     * @return ????????????
     */
    public Version getInstanceVersion(Instance instance) {

        String version = instance.getMetadata().get(PluginInfo.DEFAULT_VERSION_NAME);
        String groupId = instance.getMetadata().get(PluginInfo.DEFAULT_GROUP_ID_NAME);
        String artifactId = instance.getMetadata().get(PluginInfo.DEFAULT_ARTIFACT_ID_NAME);

        return VersionUtil.parseVersion(version, groupId, artifactId);
    }

    @Concurrent(
            value = "authentication:sync:plugin:resource:[#groupName]:[#serviceName]",
            exception = "??????????????????????????????????????????????????????",
            waitTime = @Time(0L)
    )
    public void syncPluginResource(String groupName, String serviceName, List<Instance> instances) {

        if (applicationConfig.getIgnorePluginService().contains(serviceName)) {
            return;
        }

        Optional<Instance> optional = instances.stream().max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("?????????????????? [" + groupName + "][" + serviceName + "] ?????????????????????");
            return;
        }

        Instance instance = optional.get();
        // ????????????????????????
        Version version = getInstanceVersion(instance);

        PluginInstance pluginInstance = Casts.of(instance, PluginInstance.class);
        pluginInstance.setServiceName(serviceName);
        pluginInstance.setVersion(version);
        pluginInstance.setGroup(groupName);

        List<PluginInstance> cache = instanceCache.computeIfAbsent(groupName, k -> new LinkedList<>());

        Optional<PluginInstance> exist = cache
                .stream()
                .filter(c -> c.getServiceName().equals(pluginInstance.getServiceName()))
                .findFirst();
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (exist.isPresent()) {

            PluginInstance existData = exist.get();

            if (existData.getVersion().compareTo(pluginInstance.getVersion()) > 0) {
                return;
            }

            cache.remove(existData);
        }

        Map<String, Object> info = getInstanceInfo(instance);
        pluginInstance.setInfo(info);

        cache.add(pluginInstance);

        enabledApplicationResource(pluginInstance);

    }

    /**
     * ??????????????????
     *
     * @param instance ????????????
     */
    public void enabledApplicationResource(PluginInstance instance) {

        if (Objects.isNull(instance) || Objects.isNull(instance.getVersion())) {
            return;
        }

        // ????????????
        String applicationName = instance.getServiceName();

        if (log.isDebugEnabled()) {
            log.debug("?????????????????? [" + instance.getGroup() + "] ??? [" + applicationName + " " + instance.getVersion() + "] ??????????????????");
        }

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getInfo());
        // ????????????????????????????????????
        List<ResourceMeta> newResourceList = pluginList
                .stream()
                .map(p -> createResource(p, instance, null))
                .toList();

        List<ResourceMeta> unmergeResourceList = TreeUtils.unBuildGenericTree(newResourceList);

        resources.removeIf(r -> r.getApplicationName().equals(instance.getServiceName()));
        resources.addAll(unmergeResourceList);

        if (log.isDebugEnabled()) {
            log.debug("???????????? [" + instance.getGroup() + "] ??? [" + applicationName + " " + instance.getVersion() + "] ????????????????????????");
        }

        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postSyncPlugin(instance, unmergeResourceList));
        }
    }

    /**
     * ?????? info ????????????????????????????????????
     *
     * @param info info ??????
     * @return ????????????????????????
     */
    private List<PluginInfo> createPluginInfoListFromInfo(Map<String, Object> info) {

        List<PluginInfo> result = new LinkedList<>();

        List<Map<String, Object>> pluginMapList = Casts.cast(info.get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME));

        for (Map<String, Object> pluginMap : pluginMapList) {
            PluginInfo pluginInfo = createPluginInfo(pluginMap);
            result.add(pluginInfo);
        }

        return result;
    }

    /**
     * ???????????? map ????????????????????????
     *
     * @param pluginMap ?????? map
     * @return ??????????????????
     */
    private PluginInfo createPluginInfo(Map<String, Object> pluginMap) {

        List<Map<String, Object>> children = new LinkedList<>();

        if (pluginMap.containsKey(PluginInfo.DEFAULT_CHILDREN_NAME)) {
            children = Casts.cast(pluginMap.get(PluginInfo.DEFAULT_CHILDREN_NAME));
            pluginMap.remove(PluginInfo.DEFAULT_CHILDREN_NAME);
        }

        PluginInfo pluginInfo = Casts.convertValue(pluginMap, PluginInfo.class);

        List<Tree<String, PluginInfo>> childrenNode = new LinkedList<>();

        pluginInfo.setChildren(childrenNode);

        for (Map<String, Object> child : children) {
            PluginInfo childNode = createPluginInfo(child);
            childrenNode.add(childNode);
        }

        return pluginInfo;
    }

    /**
     * ????????????
     *
     * @param plugin   ????????????
     * @param instance ????????????
     * @param parent   ????????????
     * @return ????????????
     */
    private ResourceMeta createResource(PluginInfo plugin, PluginInstance instance, ResourceMeta parent) {
        ResourceMeta target = Casts.of(
                plugin,
                ResourceMeta.class,
                IdEntity.ID_FIELD_NAME,
                PluginInfo.DEFAULT_CHILDREN_NAME,
                PluginInfo.DEFAULT_SOURCES_NAME
        );

        Assert.notEmpty(plugin.getSources(), "???????????? [" + Casts.convertValue(plugin, Map.class) + "] ??? sources ??????");

        List<ResourceSourceEnum> sources = plugin
                .getSources()
                .stream()
                .map(s -> NameEnumUtils.parse(s, ResourceSourceEnum.class))
                .toList();

        target.setSources(sources);
        target.setType(NameEnumUtils.parse(plugin.getType(), ResourceType.class));

        if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        } else if (Objects.nonNull(parent)) {
            target.setParentId(parent.getId());
        }

        if (StringUtils.isBlank(target.getApplicationName())) {
            target.setApplicationName(instance.getServiceName());
        }

        if (instance.getVersion() != null) {
            target.setVersion(instance.getVersion().toString());
        }

        target.setCode(plugin.getId());

        String id = generateId(target);
        target.setId(id);

        // ?????? target ??????????????????
        plugin.getChildren()
                .stream()
                .map(c -> createResource(Casts.cast(c, PluginInfo.class), instance, target))
                .forEach(r -> target.getChildren().add(r));

        return target;
    }

    /**
     * ???????????? id
     *
     * @param target ?????????????????????
     * @return ?????? id
     */
    private String generateId(ResourceMeta target) {
        String s = target.getApplicationName() + target.getCode() + target.getType() + target.getSources() + target.getParent();
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ????????????
     *
     * @param nacosService nacos ????????????
     */
    public void disabledApplicationResource(NacosService nacosService) {

        List<ResourceSourceEnum> sources = resources
                .stream()
                .filter(r -> r.getApplicationName().equals(nacosService.getName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .toList();

        resources.removeIf(r -> r.getApplicationName().equals(nacosService.getName()));
        // ?????????????????????????????????,????????????????????????
        authorizationService.deleteAuthorizationCache(sources);
        // ????????????????????????
        List<PluginInstance> instances = instanceCache.computeIfAbsent(nacosService.getGroupName(), k -> new LinkedList<>());
        instances.removeIf(p -> p.getServiceName().equals(nacosService.getName()));
        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postDisabledApplicationResource(nacosService));
        }
    }

    /**
     * ?????? nacos ????????????????????????????????????????????????
     *
     * @param event ????????????
     */
    @EventListener
    public void onNacosServiceSubscribeEvent(NacosServiceSubscribeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());
        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * ?????? nasoc ??????????????????
     *
     * @param event ????????????
     */
    @EventListener
    public void onNacosInstancesChangeEvent(NacosInstancesChangeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());

        if (CollectionUtils.isEmpty(nacosService.getInstances())) {
            disabledApplicationResource(nacosService);
            return;
        }

        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * ????????????????????????
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Concurrent(value = "authentication:subscribe_or_unsubscribe:plugin", exception = "????????????????????????????????????", waitTime = @Time(0L))
    public void resubscribeAllService() {
        nacosSpringEventManager.expiredAllListener();
        nacosSpringEventManager.scanThenUnsubscribeService();

        pluginServiceValidator.clearExceptionServices();

        nacosSpringEventManager.scanThenSubscribeService();
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    public List<ResourceMeta> getResources() {
        return this
                .resources
                .stream()
                .map(r -> Casts.of(r, ResourceMeta.class, PluginInfo.DEFAULT_CHILDREN_NAME))
                .toList();
    }

    /**
     * ??????????????????????????????
     *
     * @return ????????????????????????
     */
    public Set<String> getPluginServerNames() {
        return getResources().stream().collect(Collectors.groupingBy(ResourceMeta::getApplicationName)).keySet();
    }
}
