package com.github.dactiv.saas.config.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.feign.MessageServiceFeignClient;
import com.github.dactiv.saas.config.config.AttachmentConfig;
import com.github.dactiv.saas.config.domain.meta.PreviewFileMeta;
import com.github.dactiv.saas.config.enumerate.AttachmentTypeEnum;
import com.github.dactiv.saas.config.enumerate.PreviewFileTypeEnum;
import com.github.dactiv.saas.config.resolver.PreviewFileResolver;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 附件工具服务
 */
@Service
public class AttachmentService implements InitializingBean {

    @Getter
    private final AttachmentConfig attachmentConfig;

    @Getter
    private final MinioTemplate minioTemplate;

    @Getter
    private final RedissonClient redissonClient;

    private final List<PreviewFileResolver> previewFileResolvers;

    public AttachmentService(AttachmentConfig attachmentConfig,
                             MinioTemplate minioTemplate,
                             RedissonClient redissonClient,
                             ObjectProvider<PreviewFileResolver> reviewFileResolvers) {
        this.attachmentConfig = attachmentConfig;
        this.minioTemplate = minioTemplate;
        this.redissonClient = redissonClient;
        this.previewFileResolvers = reviewFileResolvers.orderedStream().collect(Collectors.toList());
    }

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target       目标对象
     * @param targetClass  目标类
     * @param ignoreFields 要忽略的字段名
     * @return map 对象
     */
    public Map<String, Object> convertFields(Object target, Class<?> targetClass, List<String> ignoreFields) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList
                .stream()
                .filter(field -> !ignoreFields.contains(field.getName()))
                .forEach(field -> result.put(field.getName(), getFieldToStringValue(target, field)));

        if (Objects.nonNull(targetClass.getSuperclass())) {
            result.putAll(convertFields(target, targetClass.getSuperclass(), ignoreFields));
        }

        return result;
    }

    public Map<String, Object> getLinkUrl(FileObject fileObject) {
        String url = MessageFormat.format(
                attachmentConfig.getResult().getLinkUri(),
                fileObject.getBucketName(),
                fileObject.getObjectName()
        );

        return Map.of(attachmentConfig.getResult().getLinkParamName(), url);
    }

    /**
     * 获取字段的 toString 值
     *
     * @param target 目标对象
     * @param field  字段
     * @return 值
     */
    private Object getFieldToStringValue(Object target, Field field) {
        Object value = ReflectionUtils.getFieldValue(target, field);

        if (Objects.isNull(value)) {
            return null;
        }

        if (StringUtils.startsWith(value.toString(), "\"") && (StringUtils.endsWith(value.toString(), "\""))) {
            value = StringUtils.unwrap(value.toString(), "\"");
        }

        return String.class.isAssignableFrom(value.getClass()) ? value : value.toString();
    }

    /**
     * 创建分片上传
     *
     * @param fileObject 文件对象
     * @param attachmentType 附件类型
     * @param contentType 内容类型
     * @param chunkSize 分片数量
     *
     * @return 创建结果
     */
    public Map<String, Object> createMultipartUpload(FileObject fileObject, AttachmentTypeEnum attachmentType, String contentType, Integer chunkSize) throws Exception {
        Assert.isTrue(chunkSize > 0, "分片数量不能小于等于 0");
        CreateMultipartUploadResponse response = getMinioTemplate().createMultipartUpload(fileObject);
        String id = response.result().uploadId();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(IntegerIdEntity.ID_FIELD_NAME, id);

        TimeProperties expiresTime = attachmentConfig.getMultipartUploadCache().getExpiresTime();

        List<String> uploadUrls = new LinkedList<>();

        Map<String, String> param = new LinkedHashMap<>();
        param.put(MinioTemplate.UPLOAD_ID_PARAM_NAME, id);
        for (int i = 1; i <= chunkSize; i++) {
            param.put(MinioTemplate.PART_NUMBER_PARAM_NAME, String.valueOf(i));
            String uploadUrl = minioTemplate.getPresignedObjectUrl(fileObject, Method.PUT, expiresTime, param);
            uploadUrls.add(uploadUrl);
        }

        result.put(MinioTemplate.CHUNK_PARAM_NAME, uploadUrls);
        result.put(HttpHeaders.CONTENT_TYPE, contentType);
        result.put(MessageServiceFeignClient.Constants.TYPE_FIELD, attachmentType.getValue());
        result.put(SystemConstants.MINIO_OBJECT_NAME, fileObject);

        String key = attachmentConfig.getMultipartUploadCache().getName(id);
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);

        bucket.setAsync(result, expiresTime.getValue(), expiresTime.getUnit());

        return result;

    }

    /**
     * 完成分片上传
     *
     * @param fileObject 文件对象
     * @param uploadId 上传 id
     */
    public Map<String, Object> completeMultipartUpload(FileObject fileObject, String uploadId) throws Exception {
        String key = attachmentConfig.getMultipartUploadCache().getName(uploadId);
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);
        Assert.isTrue(bucket.isExists(), "找不到 ID 为 [" + uploadId + "] 分片上传内容");
        Map<String, Object> map = bucket.get();
        //noinspection unchecked
        List<String> chunkList = Casts.cast(map.get(MinioTemplate.CHUNK_PARAM_NAME), List.class);

        Part[] parts = new Part[chunkList.size()];
        ListPartsResponse partResult = minioTemplate.listParts(fileObject, parts.length, uploadId);

        List<Part> partList = partResult.result().partList();
        Map<String, String> param = new LinkedHashMap<>();
        param.put(MinioTemplate.UPLOAD_ID_PARAM_NAME, uploadId);
        TimeProperties expirationTime = TimeProperties.of(1,TimeUnit.SECONDS);
        for (int i = 1; i <= partList.size(); i++) {
            parts[i - 1] = new Part(i, partList.get(i - 1).etag());
            param.put(MinioTemplate.PART_NUMBER_PARAM_NAME, String.valueOf(i));
            minioTemplate.getPresignedObjectUrl(fileObject, Method.PUT, expirationTime, param);
        }

        ObjectWriteResponse response = minioTemplate.completeMultipartUpload(fileObject, uploadId, parts);

        Map<String, Object> result = convertFields(response, response.getClass(), attachmentConfig.getResult().getUploadResultIgnoreFields());

        result.putAll(getLinkUrl(fileObject));
        result.put(HttpHeaders.CONTENT_TYPE, map.get(HttpHeaders.CONTENT_TYPE));
        result.put(MessageServiceFeignClient.Constants.TYPE_FIELD, map.get(MessageServiceFeignClient.Constants.TYPE_FIELD));

        FileObject cacheFileObject = Casts.cast(map.get(SystemConstants.MINIO_OBJECT_NAME));

        if (FilenameObject.class.isAssignableFrom(cacheFileObject.getClass())) {
            FilenameObject filenameObject = Casts.cast(cacheFileObject);
            result.put(SystemConstants.MINIO_ORIGINAL_FILE_NAME, filenameObject.getFilename());
        }

        bucket.deleteAsync();

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AttachmentTypeEnum type : AttachmentTypeEnum.values()) {
            minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(type.getValue())));
        }

        minioTemplate.makeBucketIfNotExists(SystemConstants.EXPORT_BUCKET);
    }

    public PreviewFileMeta getPreviewFile(Integer id, PreviewFileTypeEnum typeEnum, Map<String, Object> appendParam, TypeUserDetails<String> userDetails) {
        return previewFileResolvers
                .stream()
                .filter(p -> p.isSupport(typeEnum))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + typeEnum + "] 的预览文件支持"))
                .getPreviewFileMeta(id, typeEnum, appendParam, userDetails);
    }
}
