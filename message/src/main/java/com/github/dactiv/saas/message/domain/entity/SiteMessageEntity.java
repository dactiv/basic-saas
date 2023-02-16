package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.saas.commons.domain.meta.AttachmentMeta;
import com.github.dactiv.saas.message.domain.AttachmentMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息实体类</p>
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 03:48:46
 */
@Data
@NoArgsConstructor
@Alias("siteMessage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_site_message", autoResultMap = true)
public class SiteMessageEntity extends BasicMessageEntity implements AttachmentMessage, Retryable, ExecuteStatus.Body, BatchMessageEntity.Body, TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = 2037280001998945900L;

    /**
     * 标题
     */
    private String title;
    /**
     * 渠道商
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<String> channel;

    /**
     * 收信的用户 id
     */
    private Integer userId;

    /**
     * 收信的用户类型
     */
    private String userType;

    /**
     * 收信的用户名
     */
    private String username;

    /**
     * 是否可推送的消息：0.否，1.是
     */
    private YesOrNo pushable = YesOrNo.Yes;

    /**
     * 是否可读的：0.否，1.是
     */
    private YesOrNo readable = YesOrNo.Yes;

    /**
     * 读取时间
     */
    private Date readTime;

    /**
     * 状态：0.执行中、1.执行成功，2.重试中，99.执行失败
     *
     * @see ExecuteStatus
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta;
    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 0;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 批量消息 id
     */
    private Integer batchId;

    /**
     * 附件集合
     */
    @JsonCollectionGenericType(AttachmentMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<AttachmentMeta> attachmentList = new ArrayList<>();

}