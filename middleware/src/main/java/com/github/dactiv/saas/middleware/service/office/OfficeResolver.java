package com.github.dactiv.saas.middleware.service.office;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;

import java.util.Map;

/**
 * office 解析器
 *
 * @author maurice.chen
 */
public interface OfficeResolver {

    /**
     * 是否支持元数据
     *
     * @param meta 元数据
     *
     * @return true 是，否则 false
     */
    boolean isSupport(FileConvertMeta meta);

    /**
     * 转换文件元数据
     *
     * @param meta 文件转换元数据
     */
    RestResult<Map<String, Object>> convert(FileConvertMeta meta) throws Exception;
}
