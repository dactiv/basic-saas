package com.github.dactiv.saas.middleware.service.convert;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;

/**
 * 文件转换解析器接口
 *
 * @author maurice.chen
 */
public interface FileConvertResolver {
    /**
     * 是否支持来源类型
     *
     * @param meta 元数据信息
     *
     * @return true 是，否则 false
     */
    boolean isSupport(FileConvertMeta meta);

    /**
     * 转换文件
     *
     * @param meta 元数据信息
     *
     * @return reset 结果集
     */
    RestResult<Object> convert(FileConvertMeta meta);
}
