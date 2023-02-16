package com.github.dactiv.saas.commons.enumeration;

import com.github.dactiv.framework.commons.annotation.GetValueStrategy;
import com.github.dactiv.framework.commons.annotation.IgnoreField;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.spring.web.query.condition.support.SimpleConditionParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

import java.util.List;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@GetValueStrategy(type = GetValueStrategy.Type.ToString)
public enum ResourceSourceEnum implements NameValueEnum<List<String>> {

    /**
     * 管理后台
     */
    CONSOLE("系统用户", List.of(ResourceSourceEnum.CONSOLE_SOURCE_VALUE)),
    /**
     * 教师
     */
    TEACHER("教师", List.of(ResourceSourceEnum.WECHAT_TEACHER_SOURCE_VALUE, ResourceSourceEnum.MOBILE_TEACHER_SOURCE_VALUE, ResourceSourceEnum.WAKE_UP_SOURCE_VALUE, ResourceSourceEnum.TEACHER_SOURCE_VALUE)),

    /**
     * 学生
     */
    STUDENT("学生", List.of(ResourceSourceEnum.WECHAT_STUDENT_SOURCE_VALUE, ResourceSourceEnum.MOBILE_STUDENT_SOURCE_VALUE, ResourceSourceEnum.WAKE_UP_SOURCE_VALUE, ResourceSourceEnum.STUDENT_SOURCE_VALUE)),

    /**
     * 系统
     */
    @IgnoreField
    SYSTEM("系统", List.of(ResourceSourceEnum.SYSTEM_SOURCE_VALUE)),

    ;

    /**
     * 中文名称
     */
    private final String name;

    /**
     * 值
     */
    private final List<String> value;

    /**
     * 管理后台应用来源值
     */
    public static final String CONSOLE_SOURCE_VALUE = "CONSOLE";


    /**
     * 匿名用户应用来源值
     */
    public static final String ANONYMOUS_USER_SOURCE_VALUE = "ANONYMOUS_USER";


    /**
     * 系统应用来源值
     */
    public static final String SYSTEM_SOURCE_VALUE = "SYSTEM";

    /**
     * 教师应用来源值
     */
    public static final String TEACHER_SOURCE_VALUE = "TEACHER";

    /**
     * 学生应用来源值
     */
    public static final String STUDENT_SOURCE_VALUE = "STUDENT";

    /**
     * 移动端教师应用来源值
     */
    public static final String MOBILE_TEACHER_SOURCE_VALUE = "MOBILE_TEACHER";

    /**
     * 移动端学生应用来源值
     */
    public static final String MOBILE_STUDENT_SOURCE_VALUE = "MOBILE_STUDENT";

    /**
     * 微信端教师应用来源值
     */
    public static final String WECHAT_TEACHER_SOURCE_VALUE = "WECHAT_TEACHER";

    /**
     * 微信端学生应用来源值
     */
    public static final String WECHAT_STUDENT_SOURCE_VALUE = "WECHAT_STUDENT";

    /**
     * 呼醒应用来源值
     */
    public static final String WAKE_UP_SOURCE_VALUE = "WAKE_UP";

    /**
     * 获取桶名称
     *
     * @param value 值
     * @return 符合 minio 桶格式的名称
     */
    public static String getMinioBucket(String value) {
        return RegExUtils.replaceAll(value, SimpleConditionParser.DEFAULT_FIELD_CONDITION_SEPARATORS, RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE);
    }

    public static ResourceSourceEnum of(String value) {
        for (ResourceSourceEnum resourceSource : ResourceSourceEnum.values()) {
            if (resourceSource.getValue().contains(value)) {
                return resourceSource;
            }
        }
        return null;
    }

}
