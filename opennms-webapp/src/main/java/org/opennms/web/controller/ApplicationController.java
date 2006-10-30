package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.web.svclayer.AdminApplicationService;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class ApplicationController extends AbstractController {

    private AdminApplicationService m_adminApplicationService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OnmsApplication application = null;
        
        String removeApplicationIdString = request.getParameter("removeApplicationId");
        String newApplicationName = request.getParameter("newApplicationName");
        String applicationIdString = request.getParameter("applicationid");
        String editString = request.getParameter("edit");
        String ifServiceIdString = request.getParameter("ifserviceid");
        
        if (removeApplicationIdString != null) {
            m_adminApplicationService.removeApplication(removeApplicationIdString);
            
            
            return new ModelAndView(new RedirectView("/admin/applications.htm", true));
        }
        
        if (newApplicationName != null) {
            m_adminApplicationService.addNewApplication(newApplicationName);
            
            /*
             * We could be smart and take the user straight to the edit page
             * for this new application, which would be great, however it's
             * not so great if the site has a huge number of available
             * applications and they need to edit application member services
             * from the service pages.  So, we don't do it.
             */
            return new ModelAndView(new RedirectView("/admin/applications.htm", true));
        }
        
        if (applicationIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminApplicationService.performEdit(applicationIdString,
                                                      editAction,
                                                      toAdd,
                                                      toDelete);

                ModelAndView modelAndView = 
                    new ModelAndView(new RedirectView("/admin/applications.htm", true));
                modelAndView.addObject("applicationid", applicationIdString);
                modelAndView.addObject("edit", null);
                return modelAndView;
            }

            EditModel model =
                m_adminApplicationService.findApplicationAndAllMonitoredServices(applicationIdString);

            return new ModelAndView("/admin/editApplication",
                                    "model",
                                    model);
        }
        
        if (applicationIdString != null) {
            return new ModelAndView("/admin/showApplication",
                                    "model",
                                    m_adminApplicationService.getApplication(applicationIdString));
        }
        
        if (ifServiceIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminApplicationService.performServiceEdit(ifServiceIdString,
                                                       editAction,
                                                       toAdd,
                                                       toDelete);
                
                ModelAndView modelAndView = 
                    new ModelAndView(new RedirectView("/admin/applications.htm", true));
                modelAndView.addObject("ifserviceid", ifServiceIdString);
                modelAndView.addObject("edit", null);
                return modelAndView;
            }

            ServiceEditModel model =
                m_adminApplicationService.findServiceApplications(ifServiceIdString);

            return new ModelAndView("/admin/editServiceApplications",
                                    "model",
                                    model);
        }

        List<OnmsApplication> sortedApplications
            = m_adminApplicationService.findAllApplications();
        
        return new ModelAndView("/admin/applications",
                                "applications",
                                sortedApplications);
    }

    public AdminApplicationService getAdminApplicationService() {
        return m_adminApplicationService;
    }

    public void setAdminApplicationService(
            AdminApplicationService adminApplicationService) {
        m_adminApplicationService = adminApplicationService;
    }

}
