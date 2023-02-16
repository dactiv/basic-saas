package com.github.dactiv.saas.commons.feign;


import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.SystemConstants;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 资源服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = SystemConstants.SYS_RESOURCES_NAME, configuration = ResourcesServiceFeignClient.Config.class)
public interface ResourcesServiceFeignClient {

    /**
     * 上传单个附件
     *
     * @param file         文件内容
     * @param type         桶名称
     * @param requestParam 附加参数
     * @return rest 结果集
     */
    @PostMapping(value = "attachment/singleUpload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResult<Map<String, Object>> singleUploadFile(@RequestPart(value = ResourcesServiceFeignClient.Config.FILE_FIELD_NAME) MultipartFile file,
                                                     @PathVariable("type") String type,
                                                     @RequestParam Map<String, String> requestParam);

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 字节流
     */
    @GetMapping("attachment/query")
    byte[] getFile(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName);

    /**
     * 判断文件是否存在
     *
     * @param bucketName 同名称
     * @param objectName 对象名称
     * @return true 存在，否则 false
     */
    @GetMapping("attachment/isObjectExist")
    boolean isFileExist(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName);

    class Config {

        public static final String FILE_FIELD_NAME = "file";

        public static MultipartFile createMultipartFile(InputStream is, String filename, String mediaType) {
            return createMultipartFile(is, ResourcesServiceFeignClient.Config.FILE_FIELD_NAME, filename, mediaType);
        }

        public static MultipartFile createMultipartFile(InputStream is, String fieldName, String filename, String mediaType) {
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            FileItem item = factory.createItem(fieldName, mediaType, true, filename);
            OutputStream os = null;
            try {
                os = item.getOutputStream();
                IOUtils.copy(is, os);
            } catch (Exception e) {
                throw new SystemException("创建 multipart file 失败", e);
            } finally {
                IOUtils.closeQuietly(is, os);
            }
            return new CommonsMultipartFile(item);
        }

        @Bean
        public RequestInterceptor feignAuthRequestInterceptor(AuthenticationProperties properties) {
            return requestTemplate -> FeignAuthenticationConfiguration.initRequestTemplate(requestTemplate, properties);
        }

        @Bean
        public Encoder feignFormEncoder() {

            return new SpringFormEncoder();

        }
    }
}
