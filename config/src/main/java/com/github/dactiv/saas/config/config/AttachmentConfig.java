package com.github.dactiv.saas.config.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 附件配置
 *
 * @author maurice.chen
 */
@Data
@Component
@EqualsAndHashCode
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.config.app.attachment")
public class AttachmentConfig {

    /**
     * 上传文件用于创建文件夹的参数名称
     */
    private String uploadFilePrefixParamName = "prefix";

    /**
     * 必须要带 prefix 参数的类型
     */
    private List<String> uploadPrefixType = List.of("message");

    /**
     * 桶前缀
     */
    private String bucketPrefix = "dactiv.saas.resource.";

    /**
     * 响应结果集配置
     */
    private Result result = new Result();

    /**
     * 源文件名称
     */
    private String sourceField = "source";

    /**
     * 创建分片上传的过期时间
     */
    private CacheProperties multipartUploadCache = CacheProperties.of(
            "dactiv.saas:config:attachment:multipart-upload",
            TimeProperties.of(1, TimeUnit.DAYS)
    );

    /**
     * 获取桶名称
     *
     * @param type 桶类型
     * @return 桶名称
     */
    public String getBucketName(String type) {
        return bucketPrefix + type;
    }

    /**
     * 响应结果集配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Result {

        /**
         * 上传文件要忽略的响应字段
         */
        private List<String> uploadResultIgnoreFields = Collections.singletonList("headers");

        /**
         * 链接 uri
         */
        private String linkUri = "/attachment/query?bucketName={0}&objectName={1}";

        /**
         * 链接参数名称
         */
        private String linkParamName = "link";
    }
}
