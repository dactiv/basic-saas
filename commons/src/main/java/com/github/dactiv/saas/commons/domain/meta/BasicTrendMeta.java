package com.github.dactiv.saas.commons.domain.meta;

import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.saas.commons.enumeration.StatisticsCategoryEnum;
import com.github.dactiv.saas.commons.enumeration.StatisticsTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicTrendMeta extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = -9153185318284261927L;

    public static final String UNIQUE_VALUE_FIELD_NAME = "uniqueValue";

    public static final String TARGET_ID_FIELD_NAME = "targetId";

    public static final String TARGET_TYPE_FIELD_NAME = "targetType";

    /**
     * 目标 id
     */
    private Integer targetId;

    /**
     * 目标类型
     */
    private StatisticsTypeEnum targetType;

    /**
     * 唯一值
     */
    private String uniqueValue;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta = new LinkedHashMap<>();

    /**
     * 元数据唯一值
     */
    private String metaUniqueValue;

    /**
     * 创建唯一值
     *
     * @param now       当前时间
     * @param className 类名称
     * @return 唯一值
     */
    public String createUniqueValue(LocalDateTime now, String className) {
        return createUniqueValue(now, null, className);
    }

    /**
     * 创建唯一值
     *
     * @param now       当前时间
     * @param category  统计类别枚举
     * @param className 类名称
     * @return 唯一值
     */
    public String createUniqueValue(LocalDateTime now, StatisticsCategoryEnum category, String className) {
        String localDateTime = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String hashValue = localDateTime + targetId + targetType + (Objects.nonNull(category) ? category : StringUtils.EMPTY) + className;
        return DigestUtils.md5DigestAsHex(hashValue.getBytes(StandardCharsets.UTF_8));
    }

    public static BasicTrendMeta of(Integer targetId, StatisticsTypeEnum targetType) {
        BasicTrendMeta result = new BasicTrendMeta();
        result.setTargetId(targetId);
        result.setTargetType(targetType);
        return result;
    }
}
