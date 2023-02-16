package com.github.dactiv.saas.message.domain.body.notice;

import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.message.domain.entity.NoticeMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 公告消息明细响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NoticeMessageDetailResponseBody extends NoticeMessageEntity {

    @Serial
    private static final long serialVersionUID = 7058787729833830289L;

    /**
     * 上一个
     */
    private IdNameMeta previous;

    /**
     * 下一个
     */
    private IdNameMeta next;
}
