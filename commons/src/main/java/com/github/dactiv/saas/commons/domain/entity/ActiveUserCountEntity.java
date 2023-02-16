package com.github.dactiv.saas.commons.domain.entity;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.StatisticsCategoryEnum;
import com.github.dactiv.saas.commons.enumeration.StatisticsTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 活跃用户计数元数据
 *
 * @author maurice.chen
 * =
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveUserCountEntity extends BasicUserDetails<Integer> implements BasicIdentification<String> {

    @Serial
    private static final long serialVersionUID = 4679899412288864099L;

    public static final String DEFAULT_INDEX_NAME = "t_active_user_data";

    public static final String CATEGORY_FIELD_NAME = "category";

    public static final String COUNT_FIELD_NAME = "count";

    /**
     * 主键 id
     */
    private String id;

    /**
     * 计数信息
     */
    private Long count = 0L;

    /**
     * 目标类型
     */
    private StatisticsCategoryEnum category;

    /**
     * 目标类型
     */
    private StatisticsTypeEnum targetType;

    /**
     * 目标 id
     */
    private Integer targetId;

    /**
     * 唯一值
     */
    private String uniqueValue;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta;

    /**
     * 元数据唯一值
     */
    private String metaUniqueValue;

    public static ActiveUserCountEntity of(BasicUserDetails<Integer> userDetails,
                                           Integer targetId,
                                           StatisticsTypeEnum targetType,
                                           StatisticsCategoryEnum category) {
        ActiveUserCountEntity result = new ActiveUserCountEntity();

        result.setUserDetails(userDetails);
        result.setCategory(category);
        result.setTargetId(targetId);
        result.setTargetType(targetType);

        return result;
    }

    /**
     * 创建唯一值
     *
     * @param className 类名称
     * @return 唯一值
     */
    public String createUniqueValue(String className) {
        String userKey = getUserId() + getUserType();
        String hashValue = userKey + targetId + targetType + (Objects.nonNull(category) ? category : StringUtils.EMPTY) + className;
        return DigestUtils.md5DigestAsHex(hashValue.getBytes(StandardCharsets.UTF_8));
    }
}
