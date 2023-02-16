package com.github.dactiv.saas.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.Date;
import java.util.Map;

/**
 * <p>认证信息实体类</p>
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice
 * @since 2020-06-01 09:22:12
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_authentication_info", autoResultMap = true)
public class AuthenticationInfoEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    public static final String DEFAULT_INDEX = "authentication-info";

    @Serial
    private static final long serialVersionUID = 5548079224380108843L;

    private Integer id;

    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    @Version
    private Integer version;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta;

    /**
     * ip 地址
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> ipRegion;

    /**
     * 设备名称
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, String> device;

    /**
     * 同步 es 状态：0.处理中，1.成功，99.失败
     */
    @EqualsAndHashCode.Exclude
    private Integer syncStatus = ExecuteStatus.Processing.getValue();

    /**
     * 重试次数
     */
    @EqualsAndHashCode.Exclude
    private Integer retryCount = 0;

    /**
     * 备注
     */
    @EqualsAndHashCode.Exclude
    private String remark;
}