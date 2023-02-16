package com.github.dactiv.saas.message.domain.meta.site.umeng.ios;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 友盟 ios payload 实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadMeta {

    private IosPayloadApsMeta aps;

}
