package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_kit - 套件</p>
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:18:56
 */
@Data
@Alias("kit")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_kit", autoResultMap = true)
public class KitEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 859674956615976827L;

    /**
     * 名称
     */
    @NotNull
    private String title;

    /**
     * 备注
     */
    private String remark;

    /**
     * 布局内容
     */
    @NotEmpty
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Map<String, Object>> layout;

    /**
     * 类别 id
     */
    private Integer categoryId;

}