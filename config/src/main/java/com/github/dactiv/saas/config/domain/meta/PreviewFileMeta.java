package com.github.dactiv.saas.config.domain.meta;

import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.config.enumerate.PreviewFileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 预览文件响应体
 *
 * @author maurice.chen
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class PreviewFileMeta extends IntegerIdEntity {

    @Serial
    private static final long serialVersionUID = 1754599089919555486L;

    /**
     * 名称
     */
    private String name;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta;

    /**
     * 用户信息
     */
    private TypeUserDetails<Integer> user;

    /**
     * 备注
     */
    private String remark;

    /**
     * 浏览次数
     */
    private Integer viewsCount;

    /**
     * 下载次数
     */
    private Integer downloadsCount;

    /**
     * 关联资料
     */
    private List<Map<String, Object>> relatedInformation = new LinkedList<>();

    /**
     * 关联数据
     */
    private Map<String, Object> data = new LinkedHashMap<>();

    /**
     * 预览类型
     */
    private PreviewFileTypeEnum type;
}
