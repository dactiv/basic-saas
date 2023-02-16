package com.github.dactiv.saas.message.domain.body.site;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.saas.commons.domain.meta.AttachmentMeta;
import com.github.dactiv.saas.commons.domain.meta.TypeIdNameMeta;
import com.github.dactiv.saas.message.domain.AttachmentMessage;
import com.github.dactiv.saas.message.domain.entity.BasicMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SiteMessageBody extends BasicMessageEntity implements AttachmentMessage {

    @Serial
    private static final long serialVersionUID = 4341146261560926962L;

    /**
     * 标题
     */
    private String title;

    /**
     * 接收方用户
     */
    @NotEmpty
    private List<TypeIdNameMeta> toUsers = new LinkedList<>();

    /**
     * 附件
     */
    private List<AttachmentMeta> attachmentList = new ArrayList<>();

    /**
     * 是否推送消息：0.否，1.是
     */
    @NotNull
    private YesOrNo pushable;

    /**
     * 数据
     */
    private Map<String, Object> meta;
}
