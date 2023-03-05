package com.github.dactiv.saas.workflow.config;

import com.github.dactiv.saas.workflow.enumerate.ScheduleFormDateTypeEnum;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Data
@Component
@EqualsAndHashCode
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.workflow.app.schedule")
public class ScheduleConfig {

    /**
     * 自动写入日程的表单申请 id
     */
    private List<ScheduleForm> autoInsertByForm = new LinkedList<>();

    /**
     * 日程对应的流程表单配置
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @RequiredArgsConstructor(staticName = "of")
    public static class ScheduleForm {
        /**
         * 流程表单 id
         */
        @NonNull
        private Integer id;

        /**
         * 内容的键名称
         */
        @NonNull
        private String contentKey;

        /**
         * 日期类型
         */
        @NonNull
        private ScheduleFormDateTypeEnum type;
    }
}
