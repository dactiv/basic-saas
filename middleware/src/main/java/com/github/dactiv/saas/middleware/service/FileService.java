package com.github.dactiv.saas.middleware.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.service.convert.FileConvertResolver;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件转换服务
 *
 * @author maurice.chen
 */
@Service
public class FileService {

    private final List<FileConvertResolver> fileConvertResolvers;

    private final RedissonClient redissonClient;

    private final ApplicationConfig applicationConfig;

    public FileService(ObjectProvider<FileConvertResolver> fileConvertResolvers,
                       RedissonClient redissonClient,
                       ApplicationConfig applicationConfig) {
        this.fileConvertResolvers = fileConvertResolvers.orderedStream().collect(Collectors.toList());
        this.redissonClient = redissonClient;
        this.applicationConfig = applicationConfig;
    }

    public RestResult<Object> convert(FileConvertMeta meta) {

        String bucketName = this.applicationConfig.getConvertFileCache().getName(meta.getId());
        RBucket<FileConvertMeta> bucket = redissonClient.getBucket(bucketName);

        boolean success = bucket.setIfAbsent(meta);
        if (!success) {
            return RestResult.of("ID 为 [" + meta.getId() + "] 的文件转换正在执行中", HttpStatus.PROCESSING.value(), RestResult.FAIL_EXECUTE_CODE);
        }

        Optional<FileConvertResolver> optional = fileConvertResolvers
                .stream()
                .filter(c -> c.isSupport(meta))
                .findFirst();

        if (optional.isEmpty()) {
            return RestResult.of("找不单类型为 [" + meta.getFormType() + "] 的解析器", HttpStatus.INTERNAL_SERVER_ERROR.value(), RestResult.FAIL_EXECUTE_CODE);
        }

        return optional.get().convert(meta);
    }

}
