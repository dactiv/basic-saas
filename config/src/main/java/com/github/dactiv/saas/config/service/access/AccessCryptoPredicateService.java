package com.github.dactiv.saas.config.service.access;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.saas.config.dao.access.AccessCryptoPredicateDao;
import com.github.dactiv.saas.config.domain.entity.access.AccessCryptoPredicateEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_access_crypto_predicate 的业务逻辑
 *
 * <p>Table: tb_access_crypto_predicate - 访问加解密条件表</p>
 *
 * @author maurice.chen
 * @see AccessCryptoPredicateEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessCryptoPredicateService extends BasicService<AccessCryptoPredicateDao, AccessCryptoPredicateEntity> {

}
