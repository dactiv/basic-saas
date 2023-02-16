package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

/**
 * 文件转换类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileConvertTypeEnum implements NameValueEnum<String> {

    /**
     * PNG 图片
     */
    PNG(".png", "png", MediaType.IMAGE_PNG_VALUE),
    /**
     * PDF 文件
     */
    PDF(".pdf", "pdf", MediaType.APPLICATION_PDF_VALUE),
    /**
     * MP4
     */
    MP4(".mp4", "mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE);

    private final String value;

    private final String name;

    private final String mediaType;
}
