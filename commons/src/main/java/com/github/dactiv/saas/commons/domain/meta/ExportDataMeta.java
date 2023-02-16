package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.ImportExportTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 导出数据模型
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExportDataMeta extends BasicUserDetails<Integer> implements BasicIdentification<String> {
    @Serial
    private static final long serialVersionUID = 8006955473517765144L;

    /**
     * 主键 id
     */
    private String id = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 状态
     */
    private ExecuteStatus status = ExecuteStatus.Processing;

    /**
     * 导出类型
     */
    private ImportExportTypeEnum type;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta;
}
