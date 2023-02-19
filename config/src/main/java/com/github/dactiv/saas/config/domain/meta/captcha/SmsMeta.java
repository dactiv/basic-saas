package com.github.dactiv.saas.config.domain.meta.captcha;

import com.github.dactiv.saas.config.service.captcha.SimpleMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信验证码描述实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsMeta extends SimpleMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1235954873943241073L;
    /**
     * 手机号码
     */
    @Pattern(
            regexp = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$",
            message = "手机号码格式错误"
    )
    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;

    /**
     * 发送消息渠道
     */
    @NotBlank(message = "发送渠道不能为空")
    private String channel;

}
