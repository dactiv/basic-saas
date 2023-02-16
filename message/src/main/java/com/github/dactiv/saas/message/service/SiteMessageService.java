package com.github.dactiv.saas.message.service;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.message.dao.SiteMessageDao;
import com.github.dactiv.saas.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.saas.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.saas.message.service.attachment.AttachmentResolver;
import com.github.dactiv.saas.message.service.support.SiteMessageSender;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_site_message 的业务逻辑
 *
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice.chen
 * @see SiteMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SiteMessageService extends BasicService<SiteMessageDao, SiteMessageEntity> implements AttachmentResolver {

    public void deleteById(Collection<? extends Serializable> ids, SecurityUserDetails userDetails) {
        List<SiteMessageEntity> messages = get(ids);
        SecurityUserDetailsConstants.contains(new LinkedList<>(messages), userDetails);
        super.deleteById(ids);
    }

    /**
     * 计数站内信未读数量
     *
     * @param userDetails 用户 id
     * @return 按类型分组的未读数量
     */
    public Map<Integer, Long> countUnreadQuantity(SecurityUserDetails userDetails) {
        List<SiteMessageEntity> list = lambdaQuery()
                .select(IdEntity::getId, BasicMessageEntity::getType)
                .eq(SiteMessageEntity::getUserId, userDetails.getId())
                .eq(SiteMessageEntity::getUserType, userDetails.getType())
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes)
                .list();
        return list.stream().collect(Collectors.groupingBy(e -> e.getType().getValue(), Collectors.counting()));
    }

    /**
     * 阅读站内信
     *
     * @param types       站内信类型集合
     * @param userDetails 当前用户信息
     */
    public void read(List<Integer> types, SecurityUserDetails userDetails) {
        Date now = new Date();

        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .set(SiteMessageEntity::getReadable, YesOrNo.No.getValue())
                .set(SiteMessageEntity::getReadTime, now)
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes.getValue())
                .eq(SiteMessageEntity::getUserId, userDetails.getId())
                .eq(SiteMessageEntity::getUserType, userDetails.getType());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper = wrapper.in(SiteMessageEntity::getType, types);
        }

        wrapper.update();
    }

    @Override
    public String getMessageType() {
        return SiteMessageSender.DEFAULT_TYPE;
    }

    @Override
    public RestResult<Object> removeAttachment(Integer id, FileObject fileObject) {
        SiteMessageEntity entity = get(id);
        entity.getAttachmentList().removeIf(a -> a.getMeta().get(SystemConstants.MINIO_BUCKET_NAME).equals(fileObject.getBucketName()) && a.getMeta().get(SystemConstants.MINIO_OBJECT_NAME).equals(fileObject.getObjectName()));
        save(entity);
        return RestResult.ofSuccess("删除附件成功", entity);
    }

    public void read(Integer id, SecurityUserDetails userDetails) {
        SiteMessageEntity entity = get(id);
        SecurityUserDetailsConstants.equals(entity, userDetails);
        entity.setReadable(YesOrNo.No);
        entity.setReadTime(new Date());
        updateById(entity);
    }

    public void deleteRead(List<Integer> types, SecurityUserDetails userDetails) {
        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .eq(SiteMessageEntity::getUserId, userDetails.getId())
                .eq(SiteMessageEntity::getUserType, userDetails.getType())
                .eq(SiteMessageEntity::getReadable, YesOrNo.No.getValue());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper.in(BasicMessageEntity::getType, types);
        }

        wrapper.remove();
    }
}
