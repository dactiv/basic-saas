package com.github.dactiv.saas.message.domain.body.email;

import com.github.dactiv.saas.commons.domain.meta.AttachmentMeta;
import com.github.dactiv.saas.message.domain.AttachmentMessage;
import com.github.dactiv.saas.message.domain.entity.BasicMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 邮件消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailMessageBody extends BasicMessageEntity implements AttachmentMessage {

    @Serial
    private static final long serialVersionUID = -1367698344075208239L;

    /**
     * 标题
     */
    private String title;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> toEmails = new LinkedList<>();

    /**
     * 附件
     */
    private List<AttachmentMeta> attachmentList = new ArrayList<>();

}
