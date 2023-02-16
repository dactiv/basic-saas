package com.github.dactiv.saas.commons.domain.meta.middleware;

import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.enumeration.FileConvertTypeEnum;
import com.github.dactiv.saas.commons.enumeration.FileFromTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * 文件转换元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileConvertMeta extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = -2920370765677801291L;

    /**
     * 要转换的文件
     */
    @NotNull
    private String file;

    /**
     * 文件来源
     */
    @NotNull
    private FileFromTypeEnum formType;

    /**
     * 元数据类型
     */
    private Map<String, Object> meta;

    /**
     * 转换类型
     */
    @NotNull
    private FileConvertTypeEnum convertType;

    public void setConvertType(List<String> officeFileSuffixList, List<String> videoFileSuffix) {
        String filename = getMeta().getOrDefault(SystemConstants.MINIO_ORIGINAL_FILE_NAME, StringUtils.EMPTY).toString();

        boolean isOfficeFile = officeFileSuffixList
                .stream()
                .anyMatch(s -> StringUtils.endsWith(filename, s));

        boolean isVideoFile = videoFileSuffix
                .stream()
                .anyMatch(s -> StringUtils.endsWith(filename, s));

        if (isOfficeFile) {
            setConvertType(FileConvertTypeEnum.PNG);
        } else if (isVideoFile) {
            setConvertType(FileConvertTypeEnum.MP4);
        }
    }
}
