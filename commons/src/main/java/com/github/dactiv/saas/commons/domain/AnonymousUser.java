package com.github.dactiv.saas.commons.domain;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.security.entity.TypeUserDetails;

public interface AnonymousUser<T> extends TypeUserDetails<T> {

    YesOrNo getAnonymous();

}
