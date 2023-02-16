package com.github.dactiv.saas.authentication.domain.meta;

import com.github.dactiv.saas.commons.domain.meta.IdNameMeta;
import com.github.dactiv.saas.commons.enumeration.TeacherTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serial;

/**
 * 教师班级元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassGradesMeta extends IdNameMeta {

    @Serial
    private static final long serialVersionUID = -679487426538376176L;

    /**
     * 学年 id
     */
    @NotNull
    private Integer schoolYearId;

    /**
     * 学年名称
     */
    @NotNull
    private String schoolYearName;

    /**
     * 年级 id
     */
    @NotNull
    private Integer gradeId;

    /**
     * 年级名称
     */
    @NotNull
    private String gradeName;

    /**
     * 教师类型
     */
    private TeacherTypeEnum type;
}
