package com.github.dactiv.saas.config.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.config.domain.meta.LinkMeta;
import com.github.dactiv.saas.config.enumerate.CarouselType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;
import java.util.Map;


/**
 * <p>Table: tb_carousel - 轮播图</p>
 *
 * @author maurice.chen
 * @since 2022-10-21 05:01:52
 */
@Data
@NoArgsConstructor
@Alias("carousel")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_carousel", autoResultMap = true)
public class CarouselEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 1233412992103316921L;

    public static final String STATUS_TABLE_FLED_NAME = "status";

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private CarouselType type;

    /**
     * 链接地址
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private LinkMeta link;

    /**
     * 状态:10.新创建,15.已更新,20.已发布
     */
    private DataRecordStatusEnum status = DataRecordStatusEnum.NEW;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 发布时间
     */
    private Date publishTime;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> cover;
    /**
     * 备注
     */
    private String remark;

}