package com.github.dactiv.saas.commons.domain.meta;

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
import java.util.LinkedList;
import java.util.List;

/**
 * 导入数据元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImportDataMeta extends BasicUserDetails<Integer> implements BasicIdentification<String> {

    @Serial
    private static final long serialVersionUID = 9084412162349184267L;

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
     * 完成时间
     */
    private Date completeTime;

    /**
     * 导入数据类型
     */
    private ImportExportTypeEnum type;

    /**
     * 总记录数
     */
    private Integer totalCount = 0;

    /**
     * 成功记录数
     */
    private Integer successCount = 0;

    /**
     * 错误记录数
     */
    private Integer errorCount = 0;

    /**
     * 错误信息
     */
    private List<String> errorMessageList = new LinkedList<>();

    /**
     * 获取已导入的数据
     *
     * @return 已导入的数据
     */
    public Integer getImportCount() {
        return this.successCount + this.errorCount;
    }

}
