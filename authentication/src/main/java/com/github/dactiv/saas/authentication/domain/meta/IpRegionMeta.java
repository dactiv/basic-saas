package com.github.dactiv.saas.authentication.domain.meta;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;

/**
 * ip 区域原数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class IpRegionMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -357706294703499044L;

    public static final String IP_ADDRESS_NAME = "ipAddress";

    /**
     * ip 地址
     */
    @NonNull
    @NotEmpty
    private String ipAddress;
}
