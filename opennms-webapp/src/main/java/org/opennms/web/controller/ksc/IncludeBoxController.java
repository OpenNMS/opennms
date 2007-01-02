package org.opennms.web.controller.ksc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class IncludeBoxController extends AbstractController implements InitializingBean {
    private KscReportService m_kscReportService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("KSC/include-box", "reports", getKscReportService().getReportList());
    }
    
    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_kscReportService == null) {
            throw new IllegalStateException("property kscReportService must be set");
        }
    }
}
