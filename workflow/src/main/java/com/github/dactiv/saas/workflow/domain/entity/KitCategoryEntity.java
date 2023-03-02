package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_kit_category - 套件类别</p>
 *
 * @author maurice.chen
 *
 * @since 2022-06-05 11:49:12
 */
@Data
@NoArgsConstructor
@Alias("kitCategory")
@TableName("tb_kit_category")
@EqualsAndHashCode(callSuper = true)
public class KitCategoryEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -5776809443867956508L;

    public static final String DEFAULT_ICON_VALUE = "icon-category";

    /**
     * 名称
     */
    @NotNull
    private String title;

    /**
     * icon 图标
     */
    private String icon = DEFAULT_ICON_VALUE;

    /**
     * 布局内容
     */
    private String remark;

}