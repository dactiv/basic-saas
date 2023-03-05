package com.github.dactiv.saas.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.config.AbnormalAreaConfig;
import com.github.dactiv.saas.authentication.dao.AuthenticationInfoDao;
import com.github.dactiv.saas.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.saas.authentication.domain.meta.IpRegionMeta;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.commons.enumeration.MessageLinkTypeEnum;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * tb_authentication_info 的业务逻辑
 *
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice.chen
 * @see AuthenticationInfoEntity
 * @since 2021-11-25 02:42:57
 */
@Slf4j
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AuthenticationInfoService extends BasicService<AuthenticationInfoDao, AuthenticationInfoEntity> {

    private final AbnormalAreaConfig abnormalAreaConfig;

    private final MessageServiceFeignClient messageServiceFeignClient;

    public AuthenticationInfoService(AbnormalAreaConfig abnormalAreaConfig,
                                     MessageServiceFeignClient messageServiceFeignClient) {
        this.abnormalAreaConfig = abnormalAreaConfig;
        this.messageServiceFeignClient = messageServiceFeignClient;
    }

    /**
     * 验证认证信息
     *
     * @param info 认证信息
     */
    public void validAuthenticationInfo(AuthenticationInfoEntity info) {

        Wrapper<AuthenticationInfoEntity> wrapper = Wrappers
                .<AuthenticationInfoEntity>lambdaQuery()
                .eq(AuthenticationInfoEntity::getUserId, info.getUserId())
                .in(AuthenticationInfoEntity::getUserType, Collections.singletonList(info.getUserType()))
                .ne(AuthenticationInfoEntity::getId, info.getId())
                .orderByDesc(AuthenticationInfoEntity::getId);

        Page<AuthenticationInfoEntity> page = findPage(PageRequest.of(PageRequest.DEFAULT_PAGE), wrapper);

        Iterator<AuthenticationInfoEntity> iterator = page.getElements().iterator();

        AuthenticationInfoEntity last = iterator.hasNext() ? iterator.next() : null;

        if (Objects.isNull(last)) {
            return;
        }

        if (Objects.isNull(info.getIpRegion()) || Objects.isNull(last.getIpRegion())) {
            return ;
        }

        if (last.getIpRegion().get(IpRegionMeta.IP_ADDRESS_NAME).equals(info.getIpRegion().get(IpRegionMeta.IP_ADDRESS_NAME))) {
            return;
        }

        Map<String, Object> link = Map.of(
                MessageServiceFeignClient.Constants.TYPE_FIELD, MessageLinkTypeEnum.AUTHENTICATION_INFO.getValue(),
                IdEntity.ID_FIELD_NAME, info.getId()
        );
        Map<String, Object> meta = new LinkedHashMap<>(info.getDevice());
        meta.put(MessageServiceFeignClient.Constants.Site.LINK_META_FIELD, link);

        Map<String, Object> param = MessageServiceFeignClient.createPushableSiteMessage(
                Collections.singletonList(TypeIdNameMeta.ofUserDetails(info)),
                abnormalAreaConfig.getMessageType(),
                abnormalAreaConfig.getTitle(),
                abnormalAreaConfig.getSendContent(),
                meta
        );

        try {

            RestResult<Object> result = messageServiceFeignClient.send(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }

        } catch (Exception e) {
            log.error("发送站内信失败", e);
        }

    }

    /**
     * 根据用户明细获取最后一条认证信息
     *
     * @param userDetails 用户明细
     * @return 认证信息表
     */
    public AuthenticationInfoEntity getLastByUserDetails(BasicUserDetails<Integer> userDetails) {
        Wrapper<AuthenticationInfoEntity> wrapper = Wrappers
                .<AuthenticationInfoEntity>lambdaQuery()
                .eq(BasicUserDetails::getUserId, userDetails.getUserId())
                .eq(BasicUserDetails::getUserType, userDetails.getUserType());
        List<AuthenticationInfoEntity> result = findPage(PageRequest.of(1, 1), wrapper).getElements();
        return CollectionUtils.isNotEmpty(result) ? result.iterator().next() : null;
    }
}
