package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 带 id 的 RoleAuthority 实现，用于用户被分配组时存储 json 格式使用
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdRoleAuthorityMeta extends RoleAuthority implements BasicIdentification<Integer> {

    @Serial
    private static final long serialVersionUID = 5630516721501929606L;

    /**
     * 主键 id
     */
    private Integer id;

    public IdRoleAuthorityMeta(Integer id, String name, String authority) {
        super(name, authority);
        this.id = id;
    }
}
