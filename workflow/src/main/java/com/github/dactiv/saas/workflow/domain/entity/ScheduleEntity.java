package com.github.dactiv.saas.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_schedule - 日程表</p>
 *
 * @author maurice.chen
 * @since 2022-03-03 02:31:54
 */
@Data
@NoArgsConstructor
@Alias("schedule")
@TableName(value = "tb_schedule", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ScheduleEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -2665307128165075902L;

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
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 名称
     */
    private String name;

    /**
     * 日程内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Map<String, Object>> meta;

    /**
     * 状态
     */
    private DataRecordStatusEnum status = DataRecordStatusEnum.NEW;

    /**
     * 发布时间
     */
    private Date publishTime;
}