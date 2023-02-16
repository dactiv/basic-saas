package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.saas.message.enumerate.LikeOrUnlikeTargetTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>Table: tb_like_or_unlike - 点赞或非点赞记录</p>
 *
 * @author maurice.chen
 * @since 2022-09-08 04:14:58
 */
@Data
@NoArgsConstructor
@Alias("likeOrUnlike")
@TableName("tb_like_or_unlike")
@EqualsAndHashCode(callSuper = true)
public class LikeOrUnlikeEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 8905277695629573319L;

    private Integer id;

    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    @Version
    private Integer version;

    /**
     * 目标 id
     */
    private Integer targetId;

    /**
     * 目标名称
     */
    private LikeOrUnlikeTargetTypeEnum targetType;

    /**
     * 是否点赞:0.否,1.是
     */
    private YesOrNo isLike;

}