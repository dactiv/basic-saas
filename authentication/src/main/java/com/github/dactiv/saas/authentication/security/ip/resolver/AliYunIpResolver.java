package com.github.dactiv.saas.authentication.security.ip.resolver;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.saas.authentication.config.ApplicationConfig;
import com.github.dactiv.saas.authentication.domain.meta.IpRegionMeta;
import com.github.dactiv.saas.authentication.security.ip.IpResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

/**
 * 阿里云 ip 解析器实现
 *
 * @author maurice.chen
 */
@Component
public class AliYunIpResolver implements IpResolver {

    public static final String DEFAULT_TYPE = "aliYun";

    public static final String DEFAULT_URL = "https://zjip.market.alicloudapi.com/lifeservice/QueryIpAddr/query";

    private final RestTemplate restTemplate;

    private final ApplicationConfig applicationConfig;

    public AliYunIpResolver(RestTemplate restTemplate,
                            ApplicationConfig applicationConfig) {
        this.restTemplate = restTemplate;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public IpRegionMeta getIpRegionMeta(String ipAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "APPCODE " + applicationConfig.getAliYunIpResolverAppCode());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(DEFAULT_URL + "?ip=" + ipAddress, HttpMethod.GET, httpEntity, String.class);
        //noinspection unchecked
        Map<String, Object> body = Casts.readValue(response.getBody(), Map.class);
        if (!body.get("error_code").equals(0)) {
            return null;
        }

        //noinspection unchecked
        return AliYunIpRegionMeta.of(Casts.cast(body.get("result"), Map.class));
    }

    @Override
    public boolean isSupport(String type) {
        return DEFAULT_TYPE.equals(type);
    }

    /**
     * 阿里云 ip 区域元数据实现
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class AliYunIpRegionMeta extends IpRegionMeta {

        @Serial
        private static final long serialVersionUID = -8649534235422509148L;

        /**
         * 国家
         */
        private String country;

        /**
         * 省
         */
        private String province;

        /**
         * 市
         */
        private String city;

        /**
         * 区域
         */
        private String district;

        public static AliYunIpRegionMeta of(Map<String, Object> body) {
            AliYunIpRegionMeta result = new AliYunIpRegionMeta();

            result.setIpAddress(body.get("ip").toString());

            if (Objects.nonNull(body.get("country"))) {
                result.setCountry(body.get("country").toString());
            }

            if (Objects.nonNull(body.get("province"))) {
                result.setProvince(body.get("province").toString());
            }

            if (Objects.nonNull(body.get("city"))) {
                result.setCity(body.get("city").toString());
            }

            if (Objects.nonNull(body.get("district"))) {
                result.setDistrict(body.get("district").toString());
            }

            return result;
        }
    }
}
