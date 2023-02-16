package com.github.dactiv.saas.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.saas.commons.domain.BasicAnonymousUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.util.*;


/**
 * <p>Table: tb_comment_message - 评论消息</p>
 *
 * @author maurice.chen
 * @since 2022-07-01 10:59:09
 */
@Data
@NoArgsConstructor
@Alias("commentMessage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_comment_message", autoResultMap = true)
public class CommentMessageEntity extends BasicAnonymousUser<Integer> implements Tree<Integer, CommentMessageEntity>, VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -7876532955600598420L;

    public static final String CLOSE_META_KEY = "close";

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
     * 目标 id
     */
    @NotNull
    private Integer targetId;

    /**
     * 目标名称
     */
    @NotNull
    private String targetName;

    /**
     * 目标类型
     */
    @NotNull
    private String targetType;

    /**
     * 内容
     */
    @NotNull
    private String content;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta;

    /**
     * 父类 id
     */
    private Integer parentId;

    /**
     * 是否存在回复
     */
    private Integer replyCount = 0;

    /**
     * 点在数量
     */
    private Integer likeCount = 0;

    /**
     * 非点赞数量
     */
    private Integer unlikeCount = 0;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, CommentMessageEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, CommentMessageEntity> parent) {
        CommentMessageEntity group = Casts.cast(parent);
        return Objects.equals(group.getId(), this.getParent());
    }

}