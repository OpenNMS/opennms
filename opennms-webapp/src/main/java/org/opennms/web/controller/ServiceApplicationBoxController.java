package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.MissingParameterException;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Service;
import org.opennms.web.svclayer.AdminApplicationService;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ServiceApplicationBoxController extends AbstractController {
    private AdminApplicationService m_adminApplicationService; 

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request);

        List<OnmsApplication> applications = m_adminApplicationService.findByMonitoredService(service.getId());
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/serviceApplication-box", "applications",
                             applications);
        modelAndView.addObject("service", service);
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    public AdminApplicationService getAdminApplicationService() {
        return m_adminApplicationService;
    }

    public void setAdminApplicationService(AdminApplicationService adminApplicationService) {
        m_adminApplicationService = adminApplicationService;
    }

}
