package com.github.dactiv.saas.middleware.consumer;

import com.rabbitmq.client.Channel;
import com.github.dactiv.saas.commons.domain.meta.middleware.FileConvertMeta;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class BasicFileConvertConsumer {

    protected final ApplicationConfig applicationConfig;

    protected final RedissonClient redissonClient;

    protected final AmqpTemplate amqpTemplate;

    public BasicFileConvertConsumer(ApplicationConfig applicationConfig,
                                    RedissonClient redissonClient,
                                    AmqpTemplate amqpTemplate) {
        this.applicationConfig = applicationConfig;
        this.redissonClient = redissonClient;
        this.amqpTemplate = amqpTemplate;
    }

    protected RBucket<FileConvertMeta> getBucket(String id) {
        String bucketName = this.applicationConfig.getConvertFileCache().getName(id);
        return redissonClient.getBucket(bucketName);
    }

    protected FileConvertMeta getFileConvertMeta(String id, Channel channel, long tag) throws IOException {

        RBucket<FileConvertMeta> bucket = getBucket(id);
        FileConvertMeta meta = bucket.get();

        if (Objects.isNull(meta)) {
            bucket.deleteAsync();
            log.warn("找不到 ID 为 [" + id + "] 的文件转换元数据");
            channel.basicNack(tag, false, false);
            return null;
        }

        return meta;
    }
}
