package com.github.dactiv.saas.message.domain;

import com.github.dactiv.saas.commons.domain.meta.AttachmentMeta;

import java.util.List;

/**
 * 带附件的消息
 *
 * @author maurice.chen
 */
public interface AttachmentMessage {

    String ATTACHMENT_LIST_FIELD_NAME = "attachmentList";

    /**
     * 获取附件信息集合
     *
     * @return 附件信息集合
     */
    List<AttachmentMeta> getAttachmentList();
}
