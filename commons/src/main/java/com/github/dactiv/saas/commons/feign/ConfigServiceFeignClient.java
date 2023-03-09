package com.github.dactiv.saas.commons.feign;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.SystemConstants;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 管理服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_CONFIG_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface ConfigServiceFeignClient {

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{name:.*}")
    List<Map<String, Object>> findDataDictionaries(@PathVariable("name") String name);



    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     * @return 绑定 token
     */
    @PostMapping("captcha/createGenerateCaptchaIntercept")
    Map<String, Object> createGenerateCaptchaIntercept(@RequestParam("token") String token,
                                                       @RequestParam("type") String type,
                                                       @RequestParam("interceptType") String interceptType);

    /**
     * 创建绑定 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 唯一识别
     * @return 绑定 token
     */
    @GetMapping("captcha/generateToken")
    Map<String, Object> generateToken(@RequestParam("type") String type,
                                      @RequestParam("deviceIdentified") String deviceIdentified);

    /**
     * 校验验证码
     *
     * @param param 参数信息
     * @return rest 结果集
     */
    @PostMapping("captcha/verifyCaptcha")
    RestResult<Map<String, Object>> verifyCaptcha(@RequestParam Map<String, Object> param);

    /**
     * 上传单个附件
     *
     * @param file 文件内容
     * @param type 桶名称
     * @param requestParam 附加参数
     *
     * @return rest 结果集
     */
    @PostMapping(value="attachment/singleUpload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResult<Map<String, Object>> singleUploadFile(@RequestPart(value = ConfigServiceFeignClient.Config.FILE_FIELD_NAME) MultipartFile file,
                                                     @PathVariable("type") String type,
                                                     @RequestParam Map<String, String> requestParam);

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     *
     * @return 字节流
     */
    @GetMapping("attachment/query")
    byte[] getFile(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName);

    /**
     * 判断文件是否存在
     *
     * @param bucketName 同名称
     * @param objectName 对象名称
     *
     * @return true 存在，否则 false
     */
    @GetMapping("attachment/isObjectExist")
    boolean isFileExist(@RequestParam("bucketName")String bucketName, @RequestParam("objectName") String objectName);

    class Config {

        public static final String FILE_FIELD_NAME = "file";

        @Bean
        public RequestInterceptor feignAuthRequestInterceptor(AuthenticationProperties properties) {

            return requestTemplate -> FeignAuthenticationConfiguration.initRequestTemplate(requestTemplate, properties);
        }

        @Bean
        public Encoder feignFormEncoder() {

            return new SpringFormEncoder();

        }
    }

    class MockMultipartFile implements MultipartFile {

        private final String name;

        private final String originalFilename;

        @Nullable
        private final String contentType;

        private final byte[] content;


        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name    the name of the file
         * @param content the content of the file
         */
        public MockMultipartFile(String name, @Nullable byte[] content) {
            this(name, "", null, content);
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name          the name of the file
         * @param contentStream the content of the file as stream
         * @throws IOException if reading from the stream failed
         */
        public MockMultipartFile(String name, InputStream contentStream) throws IOException {
            this(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name             the name of the file
         * @param originalFilename the original filename (as on the client's machine)
         * @param contentType      the content type (if known)
         * @param content          the content of the file
         */
        public MockMultipartFile(
                String name, @Nullable String originalFilename, @Nullable String contentType, @Nullable byte[] content) {

            Assert.hasLength(name, "Name must not be empty");
            this.name = name;
            this.originalFilename = (originalFilename != null ? originalFilename : "");
            this.contentType = contentType;
            this.content = (content != null ? content : new byte[0]);
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name             the name of the file
         * @param originalFilename the original filename (as on the client's machine)
         * @param contentType      the content type (if known)
         * @param contentStream    the content of the file as stream
         * @throws IOException if reading from the stream failed
         */
        public MockMultipartFile(
                String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream)
                throws IOException {

            this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
        }


        @Override
        @NonNull
        public String getName() {
            return this.name;
        }

        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }

        @Nullable
        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return (this.content.length == 0);
        }

        @Override
        public long getSize() {
            return this.content.length;
        }

        @Override
        public byte @NonNull [] getBytes() {
            return this.content;
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.content);
        }

        @Override
        public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
            FileCopyUtils.copy(this.content, dest);
        }
    }
}
