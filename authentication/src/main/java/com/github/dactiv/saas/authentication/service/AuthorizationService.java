package com.github.dactiv.saas.authentication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.security.entity.ResourceAuthority;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.rememberme.RememberMeToken;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
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
import org.redisson.api.RBucket;
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
 * ??????????????????
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

    @Getter
    private final AuthenticationProperties authenticationProperties;

    public AuthorizationService(ObjectProvider<UserDetailsService<?>> userDetailsServices,
                                RedissonClient redissonClient,
                                PluginResourceService pluginResourceService,
                                GroupService groupService,
                                AuthenticationProperties authenticationProperties,
                                SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry) {
        this.userDetailsServices = userDetailsServices.stream().toList();
        this.redissonClient = redissonClient;
        this.pluginResourceService = pluginResourceService;
        this.groupService = groupService;
        this.authenticationProperties = authenticationProperties;
        this.sessionBackedSessionRegistry = sessionBackedSessionRegistry;
    }

    /**
     * ?????????????????? session ???????????????
     *
     * @param user ????????????
     */
    public void expireSystemUserSession(SystemUserEntity user) {
        expireUserSession(user);
    }

    private void expireUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    /**
     * ???????????? token ???
     *
     * @param user ??????????????????
     * @return ?????? token ???
     */
    private Stream<SimpleAuthenticationToken> createPrincipalAuthenticationTokenStream(SystemUserEntity user) {
        List<SimpleAuthenticationToken> result = new LinkedList<>();
        result.add(new SimpleAuthenticationToken(user.getUsername(), ResourceSourceEnum.CONSOLE.toString(), false));
        result.add(new SimpleAuthenticationToken(user.getEmail(), ResourceSourceEnum.CONSOLE.toString(), false));
        if (PhoneNumberUserDetails.class.isAssignableFrom(user.getClass())) {
            PhoneNumberUserDetails userDetails = Casts.cast(user);
            result.add(new SimpleAuthenticationToken(userDetails.getPhoneNumber(), ResourceSourceEnum.CONSOLE.toString(), false));
        }
        return result.stream();
    }

    /**
     * ????????????
     *
     * @param userDetails ????????????
     * @param oldPassword ?????????
     * @param newPassword ?????????
     */
    public void updatePassword(SecurityUserDetails userDetails, String oldPassword, String newPassword) {
        UserDetailsService<?> userDetailsService = getUserDetailsService(ResourceSourceEnum.valueOf(userDetails.getType()));
        Object target = userDetailsService.convertTargetUser(userDetails);
        userDetailsService.updatePassword(Casts.cast(target), oldPassword, newPassword);
        deleteSecurityUserDetailsCache(new SimpleAuthenticationToken(userDetails.getUsername(), userDetails.getType(), false));
    }

    /**
     * ???????????????????????????????????????
     *
     * @param source ????????????
     * @return ?????????????????????????????????
     */
    public UserDetailsService<?> getUserDetailsService(ResourceSourceEnum source) {
        return userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(source.toString()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("?????????????????? [" + source + "] ??? UserDetailsService ??????"));
    }

    /**
     * ?????????????????????
     *
     * @param group ?????????
     * @return ????????????
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
     * ????????????????????????
     *
     * @param sources ??????????????????
     */
    public void deleteAuthorizationCache(List<ResourceSourceEnum> sources) {
        List<SimpleAuthenticationToken> tokens = sources
                .stream()
                .map(s -> new SimpleAuthenticationToken("*", s.toString(), false))
                .toList();

        for (SimpleAuthenticationToken token : tokens) {
            String key = authenticationProperties.getAuthorizationCache().getName(token.getName());
            redissonClient.getBucket(key).deleteAsync();
        }
    }

    /**
     * ??????????????????
     *
     * @param applicationName ????????????
     * @param sources         ?????????????????????
     * @return ????????????
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
        createPrincipalAuthenticationTokenStream(entity).forEach(this::deleteSecurityUserDetailsCache);
    }

    /**
     * ?????????????????????????????????
     *
     * @param token              ?????? token
     */
    public void deleteSecurityUserDetailsCache(SimpleAuthenticationToken token) {
        String authenticationKey = authenticationProperties.getAuthenticationCache().getName(token.getName());
        redissonClient.getBucket(authenticationKey).deleteAsync();

        String authorizationKey = authenticationProperties.getAuthorizationCache().getName(token.getName());
        redissonClient.getBucket(authorizationKey).deleteAsync();
    }

    // -------------------------------- ???????????? -------------------------------- //

    /**
     * ????????????????????????
     *
     * @param userDetails    spring ??????????????????
     * @param type           ????????????
     * @param sourceContains ????????????
     * @return ????????????????????????
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
     * ??????????????????????????????
     *
     * @param user        ????????????
     * @param userDetails ???????????????????????????
     */
    public void setSystemUserAuthorities(SystemUserEntity user, SecurityUserDetails userDetails) {
        List<IdRoleAuthorityMeta> roleAuthorities = Casts.convertValue(user.getGroupsInfo(), new TypeReference<>() {
        });
        if (CollectionUtils.isNotEmpty(roleAuthorities)) {
            userDetails.getRoleAuthorities().addAll(roleAuthorities);
        }
        // ????????????????????????
        List<ResourceMeta> userResource = getSystemUserResource(user);
        if (CollectionUtils.isNotEmpty(userResource)) {
            // ???????????? spring security ???????????????
            List<ResourceAuthority> resourceAuthorities = userResource
                    .stream()
                    .flatMap(this::createResourceAuthoritiesStream)
                    .toList();

            userDetails.getResourceAuthorities().addAll(resourceAuthorities);
        }
    }

    /**
     * ????????????????????????
     *
     * @param user ????????????
     * @return ??????????????????
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user) {
        List<IdRoleAuthorityMeta> roleAuthorities = user.getGroupsInfo();

        List<ResourceMeta> userResource = new LinkedList<>();

        if (CollectionUtils.isEmpty(roleAuthorities)) {
            return userResource;
        }

        // ?????? id ???????????????
        List<Integer> groupIds = roleAuthorities
                .stream()
                .map(IdRoleAuthorityMeta::getId)
                .toList();

        if (CollectionUtils.isEmpty(groupIds)) {
            return userResource;
        }

        List<GroupEntity> groups = groupService.get(groupIds);

        // ????????????????????????????????????????????????????????????????????????????????????
        List<ResourceSourceEnum> groupSources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .distinct()
                .toList();

        // ????????????????????????
        groups
                .stream()
                .flatMap(g -> getResourcesStream(g.getResourceMap(), groupSources))
                .forEach(userResource::add);

        // ???????????????????????????
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

    public void deleteRememberMeCache(SimpleAuthenticationToken token) {

        String key = authenticationProperties
                .getRememberMe()
                .getCache()
                .getName(token.getName());

        RBucket<RememberMeToken> bucket = redissonClient.getBucket(key);

        if (bucket.isExists()) {
            bucket.deleteAsync();
        }
    }
}
