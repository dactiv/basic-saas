package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.commons.enumeration.DataRecordStatusEnum;
import com.github.dactiv.saas.message.enumerate.NoticeMessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_notice_message - 公告表</p>
 *
 * @author maurice.chen
 * @since 2022-03-16 03:32:05
 */
@Data
@NoArgsConstructor
@Alias("noticeMessage")
@TableName(value = "tb_notice_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class NoticeMessageEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -7431463870035051194L;

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
     * 公告类型
     */
    private NoticeMessageTypeEnum type;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 状态:10.新创建,15.更新,20.已发布
     */
    private DataRecordStatusEnum status;

    /**
     * 封面
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> cover;

    /**
     * 是否热门
     */
    private YesOrNo hot = YesOrNo.No;

    /**
     * 附件集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Map<String, Object>> attachmentList = new ArrayList<>();

}