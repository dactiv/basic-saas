package com.github.dactiv.saas.commons.domain.dto.wechat;

import com.github.dactiv.saas.commons.domain.meta.wechat.TemplateMessageMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serial;

/**
 * 公众号模版消息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OfficialTemplateMessageDto extends TemplateMessageMeta {

    @Serial
    private static final long serialVersionUID = -7898931812696704855L;

    /**
     * 模板跳转链接
     */
    private String url;

    public static OfficialTemplateMessageDto of(@NonNull String openId, @NonNull String templateId) {
        OfficialTemplateMessageDto result = new OfficialTemplateMessageDto();

        result.setOpenId(openId);
        result.setTemplateId(templateId);

        return result;
    }

    public static OfficialTemplateMessageDto of(@NonNull String openId, @NonNull String templateId, String url) {
        OfficialTemplateMessageDto result = OfficialTemplateMessageDto.of(openId, templateId);
        result.setUrl(url);

        return result;
    }
}
