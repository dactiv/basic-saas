package com.github.dactiv.saas.commons.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 班级基础信息元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherClassGradesMeta extends IdNameMeta {

    @Serial
    private static final long serialVersionUID = -176215441931948725L;

    /**
     * 学年 id
     */
    private Integer schoolYearId;

    /**
     * 学年名称
     */
    private String schoolYearName;

    /**
     * 年级 id
     */
    private Integer gradeId;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班主任 id
     */
    private Integer classTeacherId;

    /**
     * 任课老师集合
     */
    private List<IdNameMeta> teacherInfo;
}
