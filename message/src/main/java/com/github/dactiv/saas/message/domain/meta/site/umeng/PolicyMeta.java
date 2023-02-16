package com.github.dactiv.saas.message.domain.meta.site.umeng;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 友盟的消息 policy 实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PolicyMeta {

    //--------------------------------------------------------------------------------------//
    // 友盟 policy 基本配置信息开始 查看: https://developer.umeng.com/docs/67966/detail/68343
    //-------------------------------------------------------------------------------------//

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private String outBizNo;

}
