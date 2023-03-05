package com.github.dactiv.saas.config.resolver.attachment;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.config.config.AttachmentConfig;
import com.github.dactiv.saas.config.enumerate.AttachmentTypeEnum;
import com.github.dactiv.saas.config.resolver.AttachmentResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 基础文件解析器实现
 *
 * @author maurice.chen
 */
@Component
public class BasicFileResolver implements AttachmentResolver {

    private final AttachmentConfig attachmentConfig;

    public BasicFileResolver(AttachmentConfig attachmentConfig) {
        this.attachmentConfig = attachmentConfig;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isSupport(AttachmentTypeEnum attachmentType) {
        return true;
    }

    @Override
    public RestResult<Map<String, Object>> preUpload(MultipartFile file,
                                                     FileObject fileObject,
                                                     Map<String, Object> appendParam) throws IOException {

        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.preUpload(file, fileObject, appendParam);
    }

    @Override
    public RestResult<Map<String, Object>> createMultipartUpload(FileObject fileObject, Map<String, Object> appendParam) {
        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.createMultipartUpload(fileObject, appendParam);
    }

    public RestResult<Map<String, Object>> preValid(FileObject fileObject, Map<String, Object> appendParam) {

        String filePrefix = appendParam.getOrDefault(attachmentConfig.getUploadFilePrefixParamName(), StringUtils.EMPTY).toString();
        if (StringUtils.isBlank(filePrefix)) {

            List<String> prefixBucketNameList = attachmentConfig
                    .getUploadPrefixType()
                    .stream()
                    .map(attachmentConfig::getBucketName)
                    .toList();

            if (prefixBucketNameList.contains(fileObject.getBucketName()) && attachmentConfig.getUploadPrefixType().contains(fileObject.getBucketName())) {
                return RestResult.of(
                        "参数 " + attachmentConfig.getUploadFilePrefixParamName() + " 不能为空",
                        HttpStatus.BAD_REQUEST.value(),
                        RestResult.FAIL_EXECUTE_CODE
                );
            }
        }

        if (StringUtils.isNotEmpty(filePrefix)) {
            String path = StringUtils.appendIfMissing(filePrefix, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            fileObject.setObjectName(path + fileObject.getObjectName());
        }

        return null;
    }

    public static BasicUserDetails<String> getStringBasicUserDetails(SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();

        if (!authentication.isAuthenticated()) {
            return null;
        }

        Object o = authentication.getDetails();

        if (SecurityUserDetails.class.isAssignableFrom(o.getClass())) {
            SecurityUserDetails userDetails = Casts.cast(o);
            return new BasicUserDetails<>(
                    userDetails.getId().toString(),
                    SecurityUserDetailsConstants.getRealName(userDetails),
                    userDetails.getType()
            );
        } else {
            Optional<HttpServletRequest> optional = SpringMvcUtils.getHttpServletRequest();
            if (optional.isEmpty()) {
                return null;
            }
            HttpServletRequest request = optional.get();
            return new BasicUserDetails<>(
                    request.getSession().getId(),
                    SpringMvcUtils.getIpAddress(),
                    SecurityUserDetailsConstants.DEFAULT_ANONYMOUS_NAME
            );
        }
    }
}
