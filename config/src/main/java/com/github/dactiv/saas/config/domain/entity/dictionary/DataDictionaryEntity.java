package com.github.dactiv.saas.config.domain.entity.dictionary;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.config.domain.meta.DataDictionaryMeta;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * <p>数据字典实体类</p>
 * <p>Table: tb_data_dictionary - 数据字典</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("dataDictionary")
@TableName("tb_data_dictionary")
@EqualsAndHashCode(callSuper = true)
public class DataDictionaryEntity extends DataDictionaryMeta implements Tree<Integer, DataDictionaryEntity>, VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 4219144269288469584L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 256)
    private String code;

    /**
     * 是否启用:0.禁用,1.启用
     */
    @NotNull
    private DisabledOrEnabled enabled;

    /**
     * 对应字典类型
     */
    private Integer typeId;

    /**
     * 根节点为 null
     */
    private Integer parentId;

    /**
     * 顺序值
     */
    private Integer sort = Integer.MAX_VALUE / 1000000;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子类节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, DataDictionaryEntity>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

    @Override
    public boolean isChildren(Tree<Integer, DataDictionaryEntity> parent) {
        DataDictionaryEntity parentEntity = Casts.cast(parent);
        return Objects.equals(parentEntity.getId(), this.parentId);
    }
}

