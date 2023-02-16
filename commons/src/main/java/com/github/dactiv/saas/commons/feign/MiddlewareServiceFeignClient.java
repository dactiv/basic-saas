package com.github.dactiv.saas.commons.feign;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.commons.SystemConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 中间件服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = SystemConstants.SYS_MIDDLEWARE_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface MiddlewareServiceFeignClient {

    @PostMapping("file/convert")
    RestResult<Object> fileConvert(@RequestBody FileConvertMeta meta);
}
