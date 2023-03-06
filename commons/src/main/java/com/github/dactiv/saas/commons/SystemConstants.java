package com.github.dactiv.saas.commons;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.minio.Bucket;

/**
 * 系统常量
 *
 * @author maurice.chen
 */
public interface SystemConstants {

    /**
     * 默认 rabbitmq 交换机名称
     */
    String RABBITMQ_EXCHANGE = "dactiv.saas.exchange";

    /**
     * 消息系统名称
     */
    String SYS_MESSAGE_NAME = "message";

    /**
     * 权限系统名称
     */
    String SYS_AUTHENTICATION_NAME = "authentication";

    /**
     * 管理系统名称
     */
    String SYS_CONFIG_NAME = "config";

    /**
     * 配置系统名称
     */
    String SYS_GATEWAY_NAME = "gateway";

    /**
     * 工作流系统名称
     */
    String SYS_WORKFLOW_NAME = "workflow";

    /**
     * 中间件系统名称
     */
    String SYS_MIDDLEWARE_NAME = "middleware";

    /**
     * 工作流系统的默认 rabbitmq 交换机名称
     */
    String SYS_WORKFLOW_RABBITMQ_EXCHANGE = SYS_WORKFLOW_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 权限系统的默认 rabbitmq 交换机名称
     */
    String SYS_AUTHENTICATION_RABBITMQ_EXCHANGE = SYS_AUTHENTICATION_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 权限系统的默认 rabbitmq 交换机名称
     */
    String SYS_MIDDLEWARE_RABBITMQ_EXCHANGE = SYS_MIDDLEWARE_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 管理系统的默认 rabbitmq 交换机名称
     */
    String SYS_CONFIG_RABBITMQ_EXCHANGE = SYS_CONFIG_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 消息系统的默认 rabbitmq 交换机名称
     */
    String SYS_MESSAGE_RABBITMQ_EXCHANGE = SYS_MESSAGE_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 密文字段参数名
     */
    String CIPHER_TEXT_PARAM_NAME = "cipherText";

    /**
     * 代码参数名
     */
    String CODE_PARAM_NAME = "code";

    /**
     * 链接 参数名
     */
    String URL_PARAM_NAME = "url";

    /**
     * post 参数名
     */
    String POST_PARAM_NAME = "post";

    /**
     * get 参数名
     */
    String GET_PARAM_NAME = "get";

    /**
     * 导出的桶信息
     */
    Bucket EXPORT_BUCKET = Bucket.of("dactiv.saas.resource.export");

    /**
     * 替换 HTML 标签正则表达式
     */
    String REPLACE_HTML_TAG_REX = "<[.[^<]]*>";

    /**
     * 替换 quill 文本编辑
     */
    String QUILL_EMPTY_STRING = "<p><br></p>";

    /**
     * 替换 &xxx; 正则表达式
     */
    String REPLACE_SPECIAL_REX = "\\&[a-zA-Z]{1,10};";

    /**
     * minio 桶名称
     */
    String MINIO_BUCKET_NAME = "bucket";

    /**
     * minio e 标签
     */
    String MINIO_ETAG = "etag";

    String MINIO_OBJECT_ETAG = "objectEtag";

    /**
     * minio 对象名称
     */
    String MINIO_OBJECT_NAME = "object";

    /**
     * minio 原始文件名称
     */
    String MINIO_ORIGINAL_FILE_NAME = "originalFileName";

    /**
     * minio 文件名称
     */
    String MINIO_FILE_NAME = "name";

    /**
     * 已存在字段
     */
    String EXIST_NAME = "exist";

    /**
     * 消息队列名称
     */
    String NOTICE_MESSAGE_QUEUE_NAME = "messageQueueName";

    /**
     * 消息队列频道
     */
    String NOTICE_MESSAGE_EXCHANGE_NAME = "messageExchangeName";

    /**
     * 消息队列请求体名称
     */
    String NOTICE_MESSAGE_BODY_NAME = "body";

}
