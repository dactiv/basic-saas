package com.github.dactiv.saas.message.domain.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 阿里云短信元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class AliYunSmsMeta implements Serializable {

    /**
     * 阿里云短信签名字段
     */
    private String signName;

    /**
     * 阿里云短信模板
     */
    private String templateCode;

    /**
     * 阿里云短信模板变量对应的实际值
     */
    private String templateParam;

    /**
     * 上行短信扩展码
     */
    private String smsUpExtendCode;

    /**
     * 外部流水扩展字段
     */
    private String outId;
}
