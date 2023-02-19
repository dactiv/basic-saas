package com.github.dactiv.saas.message.domain.entity;

import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.saas.commons.enumeration.MessageTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 基础消息实体，用于将所有消息内容公有化使用。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicMessageEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -1167940666968537341L;

    /**
     * 类型
     *
     * @see MessageTypeEnum
     */
    @NotNull
    private MessageTypeEnum type;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

}
