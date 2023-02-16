package com.github.dactiv.saas.message.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.message.dao.EmailMessageDao;
import com.github.dactiv.saas.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.saas.message.service.attachment.AttachmentResolver;
import com.github.dactiv.saas.message.service.support.EmailMessageSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_email_message 的业务逻辑
 *
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice.chen
 * @see EmailMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EmailMessageService extends BasicService<EmailMessageDao, EmailMessageEntity> implements AttachmentResolver {

    @Override
    public String getMessageType() {
        return EmailMessageSender.DEFAULT_TYPE;
    }

    @Override
    public RestResult<Object> removeAttachment(Integer id, FileObject fileObject) {
        EmailMessageEntity entity = get(id);
        entity.getAttachmentList().removeIf(a -> a.getMeta().get(SystemConstants.MINIO_BUCKET_NAME).equals(fileObject.getBucketName()) && a.getMeta().get(SystemConstants.MINIO_OBJECT_NAME).equals(fileObject.getObjectName()));
        save(entity);
        return RestResult.ofSuccess("删除附件成功", entity);
    }
}
