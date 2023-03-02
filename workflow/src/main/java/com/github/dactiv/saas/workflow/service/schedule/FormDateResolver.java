package com.github.dactiv.saas.workflow.service.schedule;

import java.util.Date;
import java.util.Map;

/**
 * 表单时间解析器
 *
 * @author maurice.chen
 */
public interface FormDateResolver {
    /**
     * 获取开始时间
     *
     * @param applyContent 提交的表单数据内容
     *
     * @return 开始时间
     */
    Date getStartDate(Map<String, Object> applyContent);

    /**
     * 获取结束时间
     *
     * @param applyContent 提交的表单数据内容
     *
     * @return 结束时间
     */
    Date getEndDate(Map<String, Object> applyContent);
}
