package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.commons.domain.BasicAnonymousUser;
import com.github.dactiv.saas.message.enumerate.EvaluateMessageTypeEnum;
import com.github.dactiv.saas.message.domain.meta.EvaluateMessageAppendMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_evaluate_message - 评价消息</p>
 *
 * @author maurice.chen
 * @since 2022-06-30 06:08:37
 */
@Data
@NoArgsConstructor
@Alias("evaluateMessage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_evaluate_message", autoResultMap = true)
public class EvaluateMessageEntity extends BasicAnonymousUser<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -8616283550299254842L;

    /**
     * 主键 id
     */
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 目标 id
     */
    @NotNull
    private Integer targetId;

    /**
     * 目标名称
     */
    @NotNull
    private String targetName;

    /**
     * 目标类型
     */
    @NotNull
    private EvaluateMessageTypeEnum targetType;

    /**
     * 星级
     */
    @NotNull
    private Double rate;

    /**
     * 内容
     */
    @NotNull
    private String content;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta;

    /**
     * 追加内容
     */
    @JsonCollectionGenericType(EvaluateMessageAppendMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<EvaluateMessageAppendMeta> append = new LinkedList<>();

}