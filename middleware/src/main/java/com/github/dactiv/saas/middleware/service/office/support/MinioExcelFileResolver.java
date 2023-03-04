package com.github.dactiv.saas.middleware.service.office.support;


import com.github.dactiv.saas.commons.enumeration.OfficeFileTypeEnum;
import com.github.dactiv.saas.commons.feign.ConfigServiceFeignClient;
import com.github.dactiv.saas.middleware.config.ApplicationConfig;
import com.github.dactiv.saas.middleware.consumer.PdfFileConvertConsumer;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * excel 文件解析器实现
 *
 * @author maurice.chen
 */
@Component
public class MinioExcelFileResolver extends AbstractMinioOfficeResolver{

    public MinioExcelFileResolver(ConfigServiceFeignClient configServiceFeignClient,
                                  ApplicationConfig applicationConfig,
                                  PdfFileConvertConsumer pdfFileConvertConsumer) {
        super(configServiceFeignClient, applicationConfig, pdfFileConvertConsumer);
    }

    @Override
    protected boolean isSupport(OfficeFileTypeEnum type) {
        return OfficeFileTypeEnum.EXCEL.equals(type);
    }

    @Override
    protected Dispatch getDispatch(ActiveXComponent activeXComponent, OfficeFileTypeEnum fileType) {
        activeXComponent.setProperty("Visible", false);
        activeXComponent.setProperty("AutomationSecurity", new Variant(3));
        return super.getDispatch(activeXComponent, fileType);
    }

    @Override
    protected void postDispatchOpen(Dispatch dispatch, ActiveXComponent activeXComponent, OfficeFileTypeEnum officeFileType, String targetFolder) {
        Dispatch.call(dispatch, "ExportAsFixedFormat", 0, targetFolder);
    }

    @Override
    public void releaseResource(ActiveXComponent activeXComponent, Dispatch dispatch, Dispatch target) {
        if (Objects.nonNull(dispatch)) {
            Dispatch.call(dispatch, "Close");
        }
        if (Objects.nonNull(activeXComponent)) {
            activeXComponent.invoke("Quit",new Variant[] {});
        }
    }
}
