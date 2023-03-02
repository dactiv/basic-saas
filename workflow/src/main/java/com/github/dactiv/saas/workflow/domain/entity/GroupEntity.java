package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_group - 流程组表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@NoArgsConstructor
@Alias("group")
@TableName("tb_group")
@EqualsAndHashCode(callSuper = true)
public class GroupEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 1722324298113521427L;

    /**
     * 名称
     */
    private String name;

    /**
     * 父类 id
     */
    private Integer parentId;

    /**
     * 备注
     */
    private String remark;

}