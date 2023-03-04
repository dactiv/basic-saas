package com.github.dactiv.saas.middleware.service.office.support;

import com.github.dactiv.saas.commons.enumeration.OfficeFileTypeEnum;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.consumer.PdfFileConvertConsumer;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ppt 文件解析器实现
 *
 * @author maurice.chen
 */
@Component
public class MinioPowerPointFileResolver extends AbstractMinioOfficeResolver{

    public MinioPowerPointFileResolver(ConfigServiceFeignClient configServiceFeignClient,
                                       ApplicationConfig applicationConfig,
                                       PdfFileConvertConsumer pdfFileConvertConsumer) {
        super(configServiceFeignClient, applicationConfig, pdfFileConvertConsumer);
    }

    @Override
    protected boolean isSupport(OfficeFileTypeEnum type) {
        return OfficeFileTypeEnum.POWER_POINT.equals(type);
    }

    @Override
    protected void releaseResource(ActiveXComponent activeXComponent, Dispatch dispatch, Dispatch target) {
        if (Objects.nonNull(dispatch)) {
            Dispatch.call(dispatch, "Close");
        }
        if (Objects.nonNull(activeXComponent)) {
            activeXComponent.invoke("Quit");
        }
    }

    @Override
    protected void postDispatchOpen(Dispatch dispatch, ActiveXComponent activeXComponent, OfficeFileTypeEnum officeFileType, String targetFolder) {
        Dispatch.call(dispatch, "SaveAs", targetFolder, 32);
    }

}