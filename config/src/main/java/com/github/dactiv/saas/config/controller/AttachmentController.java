package com.github.dactiv.saas.config.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.minio.ObjectItem;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.saas.commons.SystemConstants;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.config.domain.body.BucketObjectItem;
import com.github.dactiv.saas.config.domain.meta.PreviewFileMeta;
import com.github.dactiv.saas.config.enumerate.AttachmentTypeEnum;
import com.github.dactiv.saas.config.enumerate.PreviewFileTypeEnum;
import com.github.dactiv.saas.config.resolver.AttachmentResolver;
import com.github.dactiv.saas.config.resolver.attachment.BasicFileResolver;
import com.github.dactiv.saas.config.service.AttachmentService;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 附件管理的控制器
 *
 * @author maurice.chen
 * @since 2022-02-16 01:48:39
 */
@Slf4j
@RestController
@RequestMapping("attachment")
@Plugin(
        name = "附件管理",
        id = "attachment",
        parent = "resources",
        icon = "icon-upload",
        type = ResourceType.Menu,
        sources = {ResourceSourceEnum.SYSTEM_SOURCE_VALUE}
)
public class AttachmentController {

    private final AttachmentService attachmentService;

    private final List<AttachmentResolver> attachmentResolvers;

    public AttachmentController(AttachmentService attachmentService,
                                ObjectProvider<AttachmentResolver> attachmentResolvers) {
        this.attachmentService = attachmentService;
        this.attachmentResolvers = attachmentResolvers.orderedStream().collect(Collectors.toList());
    }

    /**
     * 获取附件信息
     *
     * @param type     附件类型
     * @param filename 文件名称
     * @return 对象集合
     */
    @PostMapping("list")
    @PreAuthorize("isAuthenticated()")
    public List<ObjectItem> list(@RequestParam String type, String filename) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
        Bucket bucket = Bucket.of(attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()));

        ListObjectsArgs.Builder builder = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .includeUserMetadata(true)
                .recursive(true)
                .useApiVersion1(false);

        if (StringUtils.isNotBlank(filename)) {
            builder.prefix(filename);
        }

        Iterable<Result<Item>> results = attachmentService.getMinioTemplate().getMinioClient().listObjects(builder.build());

        List<ObjectItem> items = new LinkedList<>();
        for (Result<Item> result : results) {
            BucketObjectItem item = new BucketObjectItem(result.get());
            item.setBucket(bucket);
            items.add(item);
        }

        return items;
    }

    /**
     * 删除文件
     *
     * @param fileObjects 文件对象集合
     * @return reset 结果集
     * @throws Exception 删除错误时抛出
     */
    @PostMapping("delete")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@RequestBody List<FileObject> fileObjects,
                                @RequestParam Map<String, Object> appendParam) throws Exception {

        for (FileObject object : fileObjects) {
            AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(object.getBucketName(), AttachmentTypeEnum.class, true);
            if (Objects.nonNull(attachmentType)) {
                object.setBucketName(attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()));
            }

            List<AttachmentResolver> attachmentResolver = attachmentResolvers
                    .stream()
                    .filter(a -> a.isSupport(attachmentType))
                    .toList();

            FileObject fileObject = FileObject.of(
                    object.getBucketName(),
                    object.getObjectName()
            );

            for (AttachmentResolver resolver : attachmentResolver) {
                RestResult<?> result = resolver.preDelete(object.getObjectName(), fileObject, appendParam);
                if (Objects.nonNull(result) && HttpStatus.OK.value() != result.getStatus()) {
                    return result;
                }
            }

            attachmentService.getMinioTemplate().deleteObject(fileObject);

            for (AttachmentResolver resolver : attachmentResolver) {
                resolver.postDelete(object.getObjectName(), fileObject, appendParam);
            }

        }

        if (fileObjects.size() == 1) {
            return RestResult.of("删除 [" + fileObjects.iterator().next().getObjectName() + "] 成功");
        } else {
            return RestResult.of("删除 " + fileObjects.size() + " 个文件成功");
        }

    }

    /**
     * 完成分片上传
     *
     * @param type 桶类型
     * @param objectName 文件名称
     * @param id 上传 id
     *
     * @return rest 结果集
     *
     */
    @PostMapping("completeMultipartUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> completeMultipartUpload(@PathVariable("type") String type,
                                                 @RequestParam String objectName,
                                                 @RequestParam String id,
                                                 @RequestParam Map<String, Object> appendParam) throws Exception{
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
        String bucket = attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucket, objectName);

        Map<String, Object> result = attachmentService.completeMultipartUpload(fileObject, id);

        this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .forEach(e -> e.completeMultipartUpload(fileObject, result, appendParam));

        return RestResult.ofSuccess("合并 [" + objectName + "] 文件成功", result);
    }

    /**
     * 创建文件上传
     *
     * @param type 桶类型
     * @param objectName 文件对象
     * @param chunkSize 片数量
     *
     * @return rest 结果集
     *
     */
    @PostMapping("createMultipartUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, Object>> createMultipartUpload(@PathVariable("type") String type,
                                                                 @RequestParam String objectName,
                                                                 @RequestParam String contentType,
                                                                 @RequestParam Integer chunkSize,
                                                                 @RequestParam Map<String, Object> appendParam) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
        String bucket = attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucket, objectName);
        FilenameObject filenameObject = FilenameObject.of(fileObject);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();

        for (AttachmentResolver resolver : attachmentResolvers) {
            RestResult<Map<String, Object>> execute = resolver.createMultipartUpload(filenameObject, appendParam);
            if (Objects.isNull(execute)) {
                continue;
            }
            if (execute.getStatus() != HttpStatus.OK.value()) {
                return execute;
            }

            if (MapUtils.isNotEmpty(execute.getData())) {
                result.put(resolver.getKeyName(), execute.getData());
            }
        }

        Map<String, Object> data = attachmentService.createMultipartUpload(filenameObject, attachmentType, contentType, chunkSize);

        result.putAll(data);
        return RestResult.ofSuccess("创建 [" + filenameObject.getFilename() + "] 的分片信息成功", result);
    }

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param type 桶类型
     * @return reset 结果集
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("singleUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, Object>> singleUpload(MultipartFile file,
                                                        @PathVariable String type,
                                                        @RequestParam Map<String, Object> appendParam) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        String bucketName = Objects.isNull(attachmentType) ? type : attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucketName, file.getOriginalFilename());
        FilenameObject filenameObject = FilenameObject.of(fileObject);
        Map<String, Object> result = new LinkedHashMap<>();

        for (AttachmentResolver resolver : attachmentResolvers) {
            RestResult<Map<String, Object>> execute = resolver.preUpload(file, filenameObject, appendParam);
            if (Objects.isNull(execute)) {
                continue;
            }
            if (execute.getStatus() != HttpStatus.OK.value()) {
                return execute;
            } else if (MapUtils.isNotEmpty(execute.getData())) {
                result.put(resolver.getKeyName(), execute.getData());
            }
        }

        ObjectWriteResponse response = attachmentService.getMinioTemplate().putObject(
                filenameObject,
                file.getInputStream()
        );

        if (MapUtils.isEmpty(result)) {
            result = attachmentService.convertFields(response, response.getClass(), attachmentService.getAttachmentConfig().getResult().getUploadResultIgnoreFields());
            result.putAll(attachmentService.getLinkUrl(filenameObject));
        } else {
            Map<String, Object> fields = attachmentService.convertFields(response, response.getClass(), attachmentService.getAttachmentConfig().getResult().getUploadResultIgnoreFields());
            fields.putAll(attachmentService.getLinkUrl(filenameObject));
            result.put(attachmentService.getAttachmentConfig().getSourceField(), fields);
        }

        result.put(SystemConstants.MINIO_ORIGINAL_FILE_NAME, filenameObject.getFilename());
        result.put(HttpHeaders.CONTENT_TYPE, file.getContentType());

        for (AttachmentResolver resolver : attachmentResolvers) {
            resolver.postUpload(file, result, response, appendParam);
        }

        return RestResult.ofSuccess("上传完成", result);
    }

    /**
     * 上传文件
     *
     * @param files 文件
     * @param type  桶类型
     * @return reset 结果集
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<List<Map<String, Object>>> upload(@RequestParam List<MultipartFile> files,
                                                        @PathVariable String type,
                                                        @RequestParam Map<String, Object> appendParam) throws Exception {
        List<Map<String, Object>> list = new LinkedList<>();

        for (MultipartFile file : files) {
            RestResult<Map<String, Object>> result = singleUpload(file, type, appendParam);

            if (result.getStatus() != HttpStatus.OK.value()) {
                log.warn("上传文件 [" + file.getName() + "] 失败，原因为:" + result.getMessage());
                continue;
            }

            list.add(result.getData());
        }

        return RestResult.ofSuccess("上传完成", list);

    }

    /**
     * 预览文件
     *
     * @param id 主键 id
     * @param type 预览类型
     * @param appendParam 附加参数
     *
     * @return 预览文件元数据
     */
    @PostMapping("previewFile")
    public PreviewFileMeta previewFile(@RequestParam Integer id,
                                       @RequestParam Integer type,
                                       @RequestParam Map<String, Object> appendParam,
                                       @CurrentSecurityContext SecurityContext securityContext) {
        PreviewFileTypeEnum typeEnum = ValueEnumUtils.parse(type, PreviewFileTypeEnum.class);
        BasicUserDetails<String> userDetails = BasicFileResolver.getStringBasicUserDetails(securityContext);
        return attachmentService.getPreviewFile(id, typeEnum, appendParam, userDetails);
    }

    /**
     * 查看对象信息
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return 对象信息
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("info/{type}/{filename}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, String>> info(@PathVariable("type") String type,
                                                @PathVariable("filename") String filename) throws Exception {

        FileObject fileObject = FileObject.of(
                attachmentService.getAttachmentConfig().getBucketName(type),
                filename
        );

        GetObjectResponse is = attachmentService.getMinioTemplate().getObject(fileObject);
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : is.headers().names()) {
            result.put(name, is.headers().get(name));
        }

        return RestResult.ofSuccess(result);
    }

    /**
     * 获取预签署 url
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return rest 结果集
     * @throws Exception 获取失败时抛出
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("presignedUrl/{type}/{filename}")
    public RestResult<String> presignedUrl(@PathVariable("type") String type,
                                           @PathVariable("filename") String filename,
                                           @RequestParam String method,
                                           @RequestParam Map<String, Object> appendParam) throws Exception {

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        FileObject fileObject = FileObject.of(
                attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()),
                filename
        );

        String url = attachmentService.getMinioTemplate().getPresignedObjectUrl(fileObject, Method.valueOf(method));

        attachmentResolvers.forEach(a -> a.presignedUrl(fileObject, url, appendParam));

        return RestResult.ofSuccess(url);
    }

    /**
     * 查询预签署 url
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param method 签署方法
     *
     * @return rest 结果集
     * @throws Exception 查询错误时抛出
     */
    @GetMapping("queryPresignedUrl")
    public RestResult<String> queryPresignedUrl(@RequestParam String bucketName,
                                                @RequestParam String objectName,
                                                @RequestParam String method,
                                                @RequestParam Map<String, Object> appendParam) throws Exception {

        String remove = StringUtils.appendIfMissing(
                attachmentService.getAttachmentConfig().getBucketPrefix(),
                Casts.DEFAULT_DOT_SYMBOL
        );
        String name = StringUtils.removeStart(bucketName, remove);

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(name, AttachmentTypeEnum.class, true);
        FileObject fileObject;
        List<AttachmentResolver> attachmentResolvers = new LinkedList<>();
        if (Objects.nonNull(attachmentType)) {
            fileObject = FileObject.of(
                    attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()),
                    objectName
            );
            attachmentResolvers = this.attachmentResolvers
                    .stream()
                    .filter(a -> a.isSupport(attachmentType))
                    .collect(Collectors.toList());
        } else {
            fileObject = FileObject.of(bucketName, objectName);
        }

        String url = attachmentService.getMinioTemplate().getPresignedObjectUrl(fileObject, Method.valueOf(method));
        attachmentResolvers.forEach(a -> a.presignedUrl(fileObject, url, appendParam));

        return RestResult.ofSuccess(url);
    }

    /**
     * 获取文件
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return 文件流字节
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("get/{type}/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable("type") String type,
                                      @PathVariable("filename") String filename,
                                      @RequestParam Map<String, Object> appendParam) throws Exception {

        if (!SystemConstants.EXPORT_BUCKET.getBucketName().equals(type)) {
            AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
            if (Objects.nonNull(attachmentType)) {
                type = attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue());
            }
        }
        FileObject fileObject = FileObject.of(type, filename);

        return getObject(fileObject, appendParam);
    }

    /**
     * 判断对象是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return true 存在，否则 false
     */
    @GetMapping("isObjectExist")
    public boolean isObjectExist(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName) {
        return attachmentService.getMinioTemplate().isObjectExist(FileObject.of(bucketName, objectName));
    }

    /**
     * 查询文件对象
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 字节流
     * @throws Exception 查询错误时抛出
     */
    @GetMapping("query")
    public ResponseEntity<byte[]> query(@RequestParam String bucketName,
                                        @RequestParam String objectName,
                                        @RequestParam Map<String, Object> appendParam) throws Exception {
        return getObject(FileObject.of(bucketName, objectName), appendParam);
    }

    /**
     * 获取文件对象
     *
     * @param fileObject 文件对象信息
     * @return 文件字节响应实体
     */
    public ResponseEntity<byte[]> getObject(FileObject fileObject, Map<String, Object> appendParam) throws Exception {

        GetObjectResponse is = attachmentService.getMinioTemplate().getObject(fileObject);
        String contentType = is.headers().get(HttpHeaders.CONTENT_TYPE);
        HttpHeaders headers = new HttpHeaders();

        if (StringUtils.isNotEmpty(contentType)) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            headers.setContentType(mediaType);
        }

        String objectName;
        if (StringUtils.contains(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
            objectName = StringUtils.substringAfterLast(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        } else {
            objectName = fileObject.getObjectName();
        }

        String filename = appendParam.getOrDefault(SystemConstants.MINIO_ORIGINAL_FILE_NAME, StringUtils.EMPTY).toString();

        if (StringUtils.isEmpty(filename)) {
            filename = objectName;
        }

        headers.setContentDispositionFormData(
                SpringMvcUtils.DEFAULT_ATTACHMENT_NAME,
                URLEncoder.encode(filename, StandardCharsets.UTF_8)
        );
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        String remove = StringUtils.appendIfMissing(
                attachmentService.getAttachmentConfig().getBucketPrefix(),
                Casts.DEFAULT_DOT_SYMBOL
        );
        String name = StringUtils.removeStart(fileObject.getBucketName(), remove);

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(name, AttachmentTypeEnum.class, true);

        if (Objects.nonNull(attachmentType)) {
            this.attachmentResolvers
                    .stream()
                    .filter(a -> a.isSupport(attachmentType))
                    .forEach(a -> a.getObject(fileObject, objectName, data, appendParam));
        }
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

}
