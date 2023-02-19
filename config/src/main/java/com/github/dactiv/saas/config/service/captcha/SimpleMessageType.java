package com.github.dactiv.saas.config.service.captcha;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简单的消息类型实体实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMessageType implements MessageType {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private String messageType;
}
