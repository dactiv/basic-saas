package com.github.dactiv.saas.commons.domain.meta.wechat;


import com.github.dactiv.framework.commons.Casts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 微信手机号码元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class PhoneInfoMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -2392120235696483716L;

    /**
     * 用户绑定的手机号（国外手机号会有区号）
     */
    private String phoneNumber;

    /**
     * 没有区号的手机号
     */
    private String purePhoneNumber;

    /**
     * 区号
     */
    private String countryCode;

    /**
     * 数据水印
     */
    private Watermark watermark;

    public PhoneInfoMeta(Map<String, Object> body) {
        this.phoneNumber = body.get("phoneNumber").toString();
        this.purePhoneNumber = body.get("purePhoneNumber").toString();
        this.countryCode = body.get("countryCode").toString();
        Map<String, Object> watermarkData = Casts.cast(body.get("watermark"));
        if (MapUtils.isNotEmpty(watermarkData)) {
            this.watermark = new Watermark();
            watermark.setTimestamp(new Date(Casts.cast(watermarkData.get("timestamp"), Long.class)));
            watermark.setAppId(watermarkData.get("appid").toString());
        }
    }

    /**
     * 数据水印
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    static class Watermark implements Serializable {

        @Serial
        private static final long serialVersionUID = 3416682018926847215L;
        /**
         * 用户获取手机号操作的时间戳
         */
        private Date timestamp;

        /**
         * 小程序appid
         */
        private String appId;
    }
}
