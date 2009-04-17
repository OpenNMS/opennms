package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class AdminRancidUpdateController extends SimpleFormController {

    InventoryService m_inventoryService;
        
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("AdminRancidUpdateController ModelAndView onSubmit");

        AdminRancidRouterDbCommClass bean = (AdminRancidRouterDbCommClass) command;
                       
        log().debug("AdminRancidUpdateController ModelAndView onSubmit updating device["+ bean.getDeviceName() + "] group[" + bean.getGroupName() + "] status[" + bean.getStatusName()+"]");

        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
        boolean done = m_inventoryService.updateNodeOnRouterDb(bean.getGroupName(), bean.getDeviceName(), bean.getDeviceTypeName(), bean.getStatusName(),bean.getComment());
        if (!done){
            log().debug("AdminRancidUpdateController ModelAndView onSubmit error while updating status for"+ bean.getGroupName() + "/" + bean.getDeviceName());
        }
        }
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
        throws ServletException {
        log().debug("AdminRancidUpdateController initBinder");
    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}
