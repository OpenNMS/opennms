package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class AdminRancidStatusController extends SimpleFormController {

    InventoryService m_inventoryService;
    
    RWSConfig m_rwsConfig;
    
    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }
    public void setRwsConfig(RWSConfig rwsConfig) {
        log().debug("RancidStatusServlet setRwsConfig");
        m_rwsConfig = rwsConfig;
    }
    
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("AdminRancidStatusController ModelAndView onSubmit");

        AdminRancidStatusCommClass bean = (AdminRancidStatusCommClass) command;
                       
        log().debug("AdminRancidStatusController ModelAndView onSubmit setting state to device["+ bean.getDeviceName() + "] group[" + bean.getGroupName() + "] status[" + bean.getStatusName()+"]");

        boolean done = m_inventoryService.updateStatus(bean.getGroupName(), bean.getDeviceName());
        if (!done){
            log().debug("AdminRancidStatusController ModelAndView onSubmit error while updating status for"+ bean.getGroupName() + "/" + bean.getDeviceName());
        }
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
        throws ServletException {
        log().debug("AdminRancidStatusController initBinder");
    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}
