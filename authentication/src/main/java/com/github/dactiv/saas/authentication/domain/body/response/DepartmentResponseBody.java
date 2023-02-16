package com.github.dactiv.saas.authentication.domain.body.response;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.authentication.domain.entity.DepartmentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 部门响应体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DepartmentResponseBody extends DepartmentEntity {
    @Serial
    private static final long serialVersionUID = 3069656041928140571L;

    private List<BasicUserDetails<Integer>> personList;
}
