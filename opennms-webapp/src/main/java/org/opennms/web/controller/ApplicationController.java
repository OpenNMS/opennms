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
            
            
            return new ModelAndView(new RedirectView("applications.htm"));
        }
        
        if (newApplicationName != null) {
            OnmsApplication newApplication =
                m_adminApplicationService.addNewApplication(newApplicationName);
            
            return new ModelAndView(new RedirectView("applications.htm"
                                                     + "?"
                                                     + "applicationid="
                                                     + newApplication.getId()
                                                     + "&"
                                                     + "edit"));
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
                
                return new ModelAndView(new RedirectView("applications.htm"
                                                         + "?"
                                                         + "applicationid="
                                                         + applicationIdString
                                                         + "&"
                                                         + "edit"));
            }

            EditModel model =
                m_adminApplicationService.findApplicationAndAllMonitoredServices(applicationIdString);

            return new ModelAndView("/admin/editApplication",
                                    "model",
                                    model);
        }
        
        if (applicationIdString != null) {
            application = m_adminApplicationService.getApplication(applicationIdString);
            return new ModelAndView("/admin/showApplication",
                                    "application",
                                    application);
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
                
                return new ModelAndView(new RedirectView("applications.htm"
                                                         + "?"
                                                         + "ifserviceid="
                                                         + ifServiceIdString
                                                         + "&"
                                                         + "edit"));
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
