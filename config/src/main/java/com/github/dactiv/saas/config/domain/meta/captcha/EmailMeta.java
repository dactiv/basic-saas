package com.github.dactiv.saas.config.domain.meta.captcha;

import com.github.dactiv.saas.config.service.captcha.SimpleMessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 电子邮件实体
 *
 * @author maurice
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailMeta extends SimpleMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 3429703228723485142L;
    /**
     * 电子邮件
     */
    @Email(message = "电子邮件格式不正确")
    @NotBlank(message = "电子邮件不能为空")
    private String email;
}
