package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.domain.meta.workflow.AuditParticipantMeta;
import com.github.dactiv.saas.commons.enumeration.FormParticipantTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>Table: tb_form_participant - 流程表单参与者表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:59:04
 */
@Data
@NoArgsConstructor
@Alias("formParticipant")
@TableName("tb_form_participant")
@EqualsAndHashCode(callSuper = true)
public class FormParticipantEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -1562603180584681773L;

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
     * 类型:10.审批人，20.抄送人
     */
    private FormParticipantTypeEnum type;

    /**
     * 表单 id
     */
    private Integer formId;

    /**
     * 顺序值
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    public static FormParticipantEntity of(AuditParticipantMeta meta, Integer formId) {
        FormParticipantEntity entity = new FormParticipantEntity();

        entity.setFormId(formId);
        entity.setSort(meta.getSort());

        entity.setType(meta.getType());

        entity.setUsername(meta.getUsername());
        entity.setUserId(meta.getUserId());
        entity.setUserType(meta.getUserType());

        return entity;
    }

}