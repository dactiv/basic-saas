package com.github.dactiv.saas.message.domain.meta.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.saas.message.domain.meta.site.umeng.PolicyMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 友盟安卓 Policy 实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPolicyMeta extends PolicyMeta {

    private Integer maxSendNum;
}
