package com.github.dactiv.saas.middleware.controller;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.service.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 转换文件
     *
     * @param meta 转换文件元数据
     *
     * @return rest 结果集
     */
    @PostMapping("convert")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<Object> convert(@RequestBody FileConvertMeta meta) {
        return fileService.convert(meta);
    }

}
