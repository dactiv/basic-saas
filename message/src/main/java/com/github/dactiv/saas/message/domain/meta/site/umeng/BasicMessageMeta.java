package com.github.dactiv.saas.message.domain.meta.site.umeng;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础消息实体, 将 ios 和安卓的公共字段抽取在改类中
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BasicMessageMeta implements Serializable {

    //----------------------------------------------------------------------------//
    // 友盟基本配置信息开始 查看: https://developer.umeng.com/docs/67966/detail/68343
    //---------------------------------------------------------------------------//

    private String appkey;

    private String secretKey;

    private Date timestamp = new Date();

    private String type;

    private String aliasType;

    private String alias;

    private Object payload;

    private PolicyMeta policy;

    private boolean productionMode;

    private String description;
}
