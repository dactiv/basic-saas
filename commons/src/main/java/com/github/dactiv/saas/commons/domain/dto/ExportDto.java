package com.github.dactiv.saas.commons.domain.dto;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 导出 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class ExportDto<T> extends BasicUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = -6262564386975402157L;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 查询条件
     */
    private T data;

    /**
     * 缓存名称
     */
    private String cacheName;
}
