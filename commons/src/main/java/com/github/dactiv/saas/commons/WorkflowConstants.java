package com.github.dactiv.saas.commons;

/**
 * 工作流变量
 *
 * @author maurice.chen
 */
public interface WorkflowConstants {

    /**
     * 申请人名称
     */
    String APPLICANT_NAME = "申请人";

    /**
     * 提交时间名称
     */
    String SUBMISSION_TIME_NAME = "提交时间";

    /**
     * 审批配型
     */
    String AUDIT_TYPE_NAME = "审批类型";

    /**
     * 提交内容名称
     */
    String SUBMIT_CONTENT_NAME = "提交内容";

    /**
     * 撤销审核通知消息队列名称
     */
    String CANCEL_AUDIT_NOTICE_MESSAGE_QUEUE_NAME = "cancelMessageQueueName";

    /**
     * 撤销审核通知消息交换机名称
     */
    String CANCEL_AUDIT_NOTICE_MESSAGE_EXCHANGE_NAME = "cancelMessageExchangeName";
}
