package com.github.dactiv.saas.commons.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学生基础信息元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class BasicStudentMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -5437504680251100964L;

    /**
     * 学生 id
     */
    private Integer studentId;

    /**
     * 学生名称
     */
    private String studentRealName;

    /**
     * 学生学号
     */
    private String studentNumber;

}
