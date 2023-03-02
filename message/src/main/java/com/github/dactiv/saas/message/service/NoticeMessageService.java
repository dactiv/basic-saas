package com.github.dactiv.saas.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.message.config.ApplicationConfig;
import com.github.dactiv.saas.message.dao.NoticeMessageDao;
import com.github.dactiv.saas.message.domain.body.notice.HotOrNotRequestBody;
import com.github.dactiv.saas.message.domain.body.notice.NoticeMessageDetailResponseBody;
import com.github.dactiv.saas.message.domain.entity.NoticeMessageEntity;
import com.github.dactiv.saas.message.service.attachment.AttachmentResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * tb_notice_message 的业务逻辑
 *
 * <p>Table: tb_notice_message - 公告表</p>
 *
 * @author maurice.chen
 * @see NoticeMessageEntity
 * @since 2022-03-16 03:32:05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeMessageService extends BasicService<NoticeMessageDao, NoticeMessageEntity> implements AttachmentResolver {

    public final String DEFAULT_TYPE = "notice";

    private final ApplicationConfig applicationConfig;

    public NoticeMessageService(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * 发布公告
     *
     * @param ids 主键 id 集合
     * @return 已发布的公告记录
     */
    public List<Integer> publish(List<Integer> ids) {
        List<NoticeMessageEntity> list = get(ids);

        return list
                .stream()
                .filter(n -> !DataRecordStatusEnum.PUBLISH.equals(n.getStatus()))
                .peek(n -> n.setStatus(DataRecordStatusEnum.PUBLISH))
                .peek(n -> n.setPublishTime(new Date()))
                .peek(this::updateById)
                .map(NoticeMessageEntity::getId)
                .toList();
    }

    public int save(NoticeMessageEntity entity, Boolean publish) {
        boolean isNew = Objects.isNull(entity.getId());

        String subTitle = RegExUtils.replaceAll(
                entity.getContent(),
                SystemConstants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        if (subTitle.length() > applicationConfig.getMaxSubTitleLength()) {
            subTitle = subTitle.substring(0, applicationConfig.getMaxSubTitleLength());
        }

        entity.setSubTitle(subTitle);

        if (isNew) {
            entity.setStatus(DataRecordStatusEnum.NEW);
        } else {
            entity.setStatus(DataRecordStatusEnum.UPDATE);
        }

        if (publish) {
            entity.setStatus(DataRecordStatusEnum.PUBLISH);
            entity.setPublishTime(new Date());
        }

        return super.save(entity);
    }

    public NoticeMessageDetailResponseBody detail(Integer id) {

        NoticeMessageEntity entity = get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        NoticeMessageDetailResponseBody body = Casts.of(entity, NoticeMessageDetailResponseBody.class);

        Wrapper<NoticeMessageEntity> previous = Wrappers
                .<NoticeMessageEntity>lambdaQuery()
                .select(NoticeMessageEntity::getId, NoticeMessageEntity::getTitle)
                .lt(NoticeMessageEntity::getId, id)
                .orderByDesc(NoticeMessageEntity::getId);

        Wrapper<NoticeMessageEntity> next = Wrappers
                .<NoticeMessageEntity>lambdaQuery()
                .select(NoticeMessageEntity::getId, NoticeMessageEntity::getTitle)
                .gt(NoticeMessageEntity::getId, id)
                .orderByAsc(NoticeMessageEntity::getId);

        PageRequest pageRequest = PageRequest.of(PageRequest.DEFAULT_PAGE);

        Page<NoticeMessageEntity> previousPage = findPage(pageRequest, next);
        if (CollectionUtils.isNotEmpty(previousPage.getElements())) {
            NoticeMessageEntity message = previousPage.getElements().iterator().next();
            body.setPrevious(IdNameMeta.of(message.getId(), message.getTitle()));
        }

        Page<NoticeMessageEntity> nextPage = findPage(pageRequest, previous);
        if (CollectionUtils.isNotEmpty(nextPage.getElements())) {
            NoticeMessageEntity message = nextPage.getElements().iterator().next();
            body.setNext(IdNameMeta.of(message.getId(), message.getTitle()));
        }

        return body;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    public RestResult<Object> removeAttachment(Integer id, FileObject fileObject) {
        NoticeMessageEntity entity = get(id);
        entity.getAttachmentList().removeIf(a -> a.get(SystemConstants.MINIO_BUCKET_NAME).equals(fileObject.getBucketName()) && a.get(SystemConstants.MINIO_OBJECT_NAME).equals(fileObject.getObjectName()));
        save(entity);
        return RestResult.ofSuccess("删除附件成功", entity);
    }

    public void hotOrNot(HotOrNotRequestBody body) {

        if (CollectionUtils.isNotEmpty(body.getHot())) {
            lambdaUpdate()
                    .set(NoticeMessageEntity::getHot, YesOrNo.Yes.getValue())
                    .in(NoticeMessageEntity::getId, body.getHot())
                    .update();
        }

        if (CollectionUtils.isNotEmpty(body.getNotHot())) {
            lambdaUpdate()
                    .set(NoticeMessageEntity::getHot, YesOrNo.No.getValue())
                    .in(NoticeMessageEntity::getId, body.getNotHot())
                    .update();
        }
    }
}
