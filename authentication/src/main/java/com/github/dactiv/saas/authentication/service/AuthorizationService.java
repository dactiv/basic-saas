package com.github.dactiv.saas.authentication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.security.entity.ResourceAuthority;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.GroupEntity;
import com.github.dactiv.saas.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.saas.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.saas.authentication.plugin.PluginResourceService;
import com.github.dactiv.saas.commons.domain.meta.IdRoleAuthorityMeta;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 授权管理服务
 *
 * @author maurice.chen
 **/
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AuthorizationService {

    private final List<UserDetailsService<?>> userDetailsServices;

    @Getter
    private final RedissonClient redissonClient;

    private final GroupService groupService;
    @Getter
    private final PluginResourceService pluginResourceService;

    private final SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;


    public AuthorizationService(ObjectProvider<UserDetailsService<?>> userDetailsServices,
                                RedissonClient redissonClient,
                                PluginResourceService pluginResourceService,
                                GroupService groupService,
                                SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry) {
        this.userDetailsServices = userDetailsServices.stream().toList();
        this.redissonClient = redissonClient;
        this.pluginResourceService = pluginResourceService;
        this.groupService = groupService;
        this.sessionBackedSessionRegistry = sessionBackedSessionRegistry;
    }

    /**
     * 将用户的所有 session 设置为超时
     *
     * @param user 用户实体
     */
    public void expireSystemUserSession(SystemUserEntity user) {
        expireUserSession(user);
    }

    private void expireUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    /**
     * 创建授权 token 流
     *
     * @param user 后台用户实体
     * @return 授权 token 流
     */
    private Stream<PrincipalAuthenticationToken> createPrincipalAuthenticationTokenStream(SystemUserEntity user) {
        List<PrincipalAuthenticationToken> result = new LinkedList<>();
        result.add(new PrincipalAuthenticationToken(user.getUsername(), ResourceSourceEnum.CONSOLE.toString(), false));
        result.add(new PrincipalAuthenticationToken(user.getEmail(), ResourceSourceEnum.CONSOLE.toString(), false));
        if (PhoneNumberUserDetails.class.isAssignableFrom(user.getClass())) {
            PhoneNumberUserDetails userDetails = Casts.cast(user);
            result.add(new PrincipalAuthenticationToken(userDetails.getPhoneNumber(), ResourceSourceEnum.CONSOLE.toString(), false));
        }
        return result.stream();
    }

    /**
     * 修改密码
     *
     * @param userDetails 当前用户
     * @param oldPassword 就密码
     * @param newPassword 新密码
     */
    public void updatePassword(SecurityUserDetails userDetails, String oldPassword, String newPassword) {
        UserDetailsService<?> userDetailsService = getUserDetailsService(ResourceSourceEnum.valueOf(userDetails.getType()));
        Object target = userDetailsService.convertTargetUser(userDetails);
        userDetailsService.updatePassword(Casts.cast(target), oldPassword, newPassword);
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param source 资源累袁
     * @return 账户认证的用户明细服务
     */
    public UserDetailsService<?> getUserDetailsService(ResourceSourceEnum source) {
        return userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(source.toString()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为 [" + source + "] 的 UserDetailsService 实现"));
    }

    /**
     * 获取组资源集合
     *
     * @param group 组信息
     * @return 资源结婚
     */
    public List<ResourceMeta> getGroupResource(GroupEntity group) {
        List<ResourceMeta> result = new LinkedList<>();

        if (MapUtils.isEmpty(group.getResourceMap())) {
            return result;
        }

        for (Map.Entry<String, List<String>> entry : group.getResourceMap().entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());
            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .toList();

            result.addAll(findResources);
        }

        return result;
    }

    public SystemUserEntity getSystemUserEntity(SecurityUserDetails userDetails) {
        ResourceSourceEnum source = ResourceSourceEnum.valueOf(userDetails.getType());
        UserDetailsService<?> userDetailsService = getUserDetailsService(source);
        return Casts.cast(userDetailsService.convertTargetUser(userDetails));
    }

    /**
     * 删除所有认证缓存
     *
     * @param sources 资源来源枚举
     */
    public void deleteAuthorizationCache(List<ResourceSourceEnum> sources) {
        List<PrincipalAuthenticationToken> tokens = sources
                .stream()
                .map(s -> new PrincipalAuthenticationToken("*", s.toString(), false))
                .toList();

        for (PrincipalAuthenticationToken token : tokens) {
            UserDetailsService<?> userDetailsService = getUserDetailsService(ResourceSourceEnum.CONSOLE);
            CacheProperties cache = userDetailsService.getAuthorizationCache(token);
            redissonClient.getBucket(cache.getName()).deleteAsync();
        }
    }

    /**
     * 获取资源集合
     *
     * @param applicationName 应用名称
     * @param sources         符合来源的记录
     * @return 资源集合
     */
    public List<ResourceMeta> getResources(String applicationName, ResourceSourceEnum... sources) {
        List<ResourceMeta> result = pluginResourceService.getResources();
        Stream<ResourceMeta> stream = result.stream();

        if (StringUtils.isNotBlank(applicationName)) {
            stream = stream.filter(r -> r.getApplicationName().equals(applicationName));
        }

        if (ArrayUtils.isNotEmpty(sources)) {
            List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
            stream = stream.filter(r -> r.getSources().stream().anyMatch(sourceList::contains));
        }

        return stream.sorted(Comparator.comparing(ResourceMeta::getSort).reversed()).toList();
    }

    public void deleteSystemUserAuthenticationCache(SystemUserEntity entity) {
        createPrincipalAuthenticationTokenStream(entity).forEach(this::deleteAuthenticationCache);
    }

    /**
     * 删除系统用户的认证缓存
     *
     * @param token 认证 token
     */
    private void deleteAuthenticationCache(PrincipalAuthenticationToken token) {

        UserDetailsService<?> userDetailsService = getUserDetailsService(ResourceSourceEnum.valueOf(token.getType()));

        deleteAuthenticationCache(userDetailsService, token);
    }

    /**
     * 删除系统用户的认证缓存
     *
     * @param userDetailsService 用户明细服务
     * @param token              认证 token
     */
    public void deleteAuthenticationCache(UserDetailsService<?> userDetailsService, PrincipalAuthenticationToken token) {
        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        if (Objects.nonNull(authenticationCache)) {

            redissonClient.getBucket(authenticationCache.getName()).deleteAsync();
        }

        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token);

        if (Objects.nonNull(authorizationCache)) {

            redissonClient.getBucket(authorizationCache.getName()).deleteAsync();
        }
    }

    // -------------------------------- 资源管理 -------------------------------- //

    /**
     * 获取系统用户资源
     *
     * @param userDetails    spring 安全用户明细
     * @param type           资源类型
     * @param sourceContains 资源来源
     * @return 系统用户资源集合
     */
    public List<ResourceMeta> getSystemUserResource(SecurityUserDetails userDetails,
                                                    ResourceType type,
                                                    List<ResourceSourceEnum> sourceContains) {

        SystemUserEntity user = getSystemUserEntity(userDetails);

        List<ResourceMeta> userResource = getSystemUserResource(user);

        Map<String, List<String>> ignoreResourceMap = pluginResourceService.getApplicationConfig().getIgnorePrincipalResource();

        if (MapUtils.isNotEmpty(ignoreResourceMap)) {
            List<ResourceMeta> removeList = new LinkedList<>();
            for (Map.Entry<String, List<String>> entry : ignoreResourceMap.entrySet()) {
                userResource
                        .stream()
                        .filter(r -> entry.getKey().equals(r.getApplicationName()))
                        .filter(r -> entry.getValue().contains(r.getCode()))
                        .forEach(removeList::add);
            }
            userResource.removeAll(removeList);
        }

        Stream<ResourceMeta> stream = userResource
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));

        if (Objects.nonNull(type)) {
            stream = stream.filter(r -> r.getType().equals(type));
        }

        return stream.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResourceMeta::getId))), ArrayList::new));
    }

    /**
     * 设置系统用户权限信息
     *
     * @param user        系统用户
     * @param userDetails 当前的安全用户明细
     */
    public void setSystemUserAuthorities(SystemUserEntity user, SecurityUserDetails userDetails) {
        List<IdRoleAuthorityMeta> roleAuthorities = Casts.convertValue(user.getGroupsInfo(), new TypeReference<>() {
        });
        if (CollectionUtils.isNotEmpty(roleAuthorities)) {
            userDetails.getRoleAuthorities().addAll(roleAuthorities);
        }
        // 构造用户的组资源
        List<ResourceMeta> userResource = getSystemUserResource(user);
        if (CollectionUtils.isNotEmpty(userResource)) {
            // 构造对应 spring security 的资源内容
            List<ResourceAuthority> resourceAuthorities = userResource
                    .stream()
                    .flatMap(this::createResourceAuthoritiesStream)
                    .toList();

            userDetails.getResourceAuthorities().addAll(resourceAuthorities);
        }
    }

    /**
     * 获取系统用户资源
     *
     * @param user 系统用户
     * @return 系统用户资源
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user) {
        List<IdRoleAuthorityMeta> roleAuthorities = user.getGroupsInfo();

        List<ResourceMeta> userResource = new LinkedList<>();

        if (CollectionUtils.isEmpty(roleAuthorities)) {
            return userResource;
        }

        // 通过 id 获取组信息
        List<Integer> groupIds = roleAuthorities
                .stream()
                .map(IdRoleAuthorityMeta::getId)
                .toList();

        if (CollectionUtils.isEmpty(groupIds)) {
            return userResource;
        }

        List<GroupEntity> groups = groupService.get(groupIds);

        // 获取组来源，用于过滤组的资源里有存在不同的资源来源细腻些
        List<ResourceSourceEnum> groupSources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .distinct()
                .toList();

        // 构造用户的组资源
        userResource = groups
                .stream()
                .flatMap(g -> getResourcesStream(g.getResourceMap(), groupSources))
                .toList();

        // 构造用户的独立资源
        userResource.addAll(getResourcesStream(user.getResourceMap(), groupSources).distinct().toList());

        return userResource;
    }

    private Stream<ResourceMeta> getResourcesStream(Map<String, List<String>> resourceMap, List<ResourceSourceEnum> sources) {

        if (MapUtils.isEmpty(resourceMap)) {
            return Stream.empty();
        }

        List<ResourceMeta> result = new LinkedList<>();

        for (Map.Entry<String, List<String>> entry : resourceMap.entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());

            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .filter(r -> r.getSources().stream().anyMatch(sources::contains))
                    .toList();

            result.addAll(findResources);
        }

        return result.stream().distinct();
    }

    private Stream<ResourceAuthority> createResourceAuthoritiesStream(ResourceMeta resource) {
        if (StringUtils.isBlank(resource.getAuthority())) {
            return Stream.empty();
        }

        String[] permissions = StringUtils.substringsBetween(
                resource.getAuthority(),
                ResourceAuthority.DEFAULT_RESOURCE_PREFIX,
                ResourceAuthority.DEFAULT_RESOURCE_SUFFIX
        );

        if (ArrayUtils.isEmpty(permissions)) {
            return Stream.empty();
        }

        return Arrays
                .stream(permissions)
                .map(ResourceAuthority::getPermissionValue)
                .map(p -> new ResourceAuthority(p, resource.getName(), resource.getValue()));
    }
}
