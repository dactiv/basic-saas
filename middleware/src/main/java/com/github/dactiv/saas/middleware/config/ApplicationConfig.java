package com.github.dactiv.saas.middleware.config;

import com.github.dactiv.framework.commons.CacheProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("cmis.middleware.app")
public class ApplicationConfig {

    /**
     * office 下载临时文件路径
     */
    private String officeDownloadPath = "C:" + File.separator + "office";

    /**
     * 视频下载临时文件路径
     */
    private String videoDownloadPath = "C:" + File.separator + "video";

    /**
     * 下载源文件后的文件名称
     */
    private String downloadSourceName = "source";

    /**
     * 转换文件后的目标文件名称
     */
    private String convertTargetName = "target";

    /**
     * 文件转换缓存配置
     */
    private CacheProperties convertFileCache = CacheProperties.of("cmis:middleware:convert-file:");

    /**
     * pdf 渲染图片 dpi 值
     */
    private int pdfRenderImageDpi = 100;

    /**
     * 文件转换成图片的文件夹名称
     */
    private String imageFolderName = "image";

    /**
     * 视频转换成指定格式的后缀名
     */
    private List<String> supportVideoFileSuffixList = List.of(".3gp", ".avi", ".f4v", ".flv", ".m4v", ".mkv", ".mov", ".mpeg", ".wmv");

}
