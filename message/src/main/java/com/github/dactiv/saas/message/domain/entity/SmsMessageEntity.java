package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.saas.message.domain.meta.AliYunSmsMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;

/**
 * <p>短信消息实体类</p>
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("smsMessage")
@TableName(value = "tb_sms_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class SmsMessageEntity extends BasicMessageEntity implements ExecuteStatus.Body, BatchMessageEntity.Body {

    @Serial
    private static final long serialVersionUID = 3229810529789017287L;

    /**
     * 渠道名称
     */
    private String channel;

    /**
     * 手机号码
     */
    private String phoneNumber;

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
     * 状态：0.执行中、1.执行成功，2.重试中，99.执行失败
     *
     * @see ExecuteStatus
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

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
     * 阿里云
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private AliYunSmsMeta aliYunMeta;

}