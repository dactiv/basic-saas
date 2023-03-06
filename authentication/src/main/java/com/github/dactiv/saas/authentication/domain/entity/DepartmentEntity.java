package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.saas.authentication.enumerate.DepartmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * <p>Table: tb_department - 部门表</p>
 *
 * @author maurice.chen
 * @since 2022-02-09 06:47:53
 */
@Data
@NoArgsConstructor
@Alias("department")
@TableName("tb_department")
@EqualsAndHashCode(callSuper = true)
public class DepartmentEntity extends IntegerVersionEntity<Integer> implements Tree<Integer, DepartmentEntity> {

    @Serial
    private static final long serialVersionUID = 6607927907369680571L;

    /**
     * 名称
     */
    private String name;

    /**
     * 父类 ID
     */
    private Integer parentId;

    /**
     * 人员总数
     */
    private Integer count = 0;

    /**
     * 部门类型
     */
    private DepartmentTypeEnum type = DepartmentTypeEnum.CONSOLE_USER;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, DepartmentEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, DepartmentEntity> parent) {
        DepartmentEntity group = Casts.cast(parent);
        return Objects.equals(group.getId(), this.getParent());
    }
}