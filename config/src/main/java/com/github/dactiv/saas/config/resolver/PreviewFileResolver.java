package com.github.dactiv.saas.config.resolver;

import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.config.domain.meta.PreviewFileMeta;
import com.github.dactiv.saas.config.enumerate.PreviewFileTypeEnum;

import java.util.Map;

/**
 * 预览文件解析器
 *
 * @author maurice.chen
 */
public interface PreviewFileResolver {

    /**
     * 是否支持预览
     *
     * @param type 预览文件类型
     *
     * @return true 是，否则 false
     */
    boolean isSupport(PreviewFileTypeEnum type);

    /**
     * 获取预览文件内容
     *
     * @param id 主键 id
     * @param type 预览文件类型
     * @param appendParam 附加参数
     * @param userDetails 当前用户
     *
     * @return 预览文件响应体
     */
    PreviewFileMeta getPreviewFileMeta(Integer id, PreviewFileTypeEnum type, Map<String, Object> appendParam, TypeUserDetails<String> userDetails);
}
