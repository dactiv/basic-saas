package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Map;


/**
 * <p>Table: tb_attachment - 附件表</p>
 *
 * @author maurice.chen
 * @since 2022-02-16 01:48:39
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class AttachmentMeta implements BasicIdentification<String> {

    @Serial
    private static final long serialVersionUID = 328652664219740407L;


    private String id;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * 媒体内容类型
     */
    @NotNull
    private String contentType;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta;

    /**
     * 目标 id
     */
    private Integer targetId;

}