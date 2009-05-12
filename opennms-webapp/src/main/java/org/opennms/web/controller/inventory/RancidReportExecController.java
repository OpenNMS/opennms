package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class RancidReportExecController extends SimpleFormController {
    
    InventoryService m_inventoryService;
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("RancidReportExecController ModelAndView onSubmit");
        
        RancidReportExecCommClass bean = (RancidReportExecCommClass) command;
        
        log().debug("RancidReportExecController ModelAndView type" + bean.getReporttype());
        log().debug("RancidReportExecController ModelAndView type" + bean.getFieldhas());

        
        if (bean.getReporttype().compareTo("rancidlist") == 0){
            log().debug("RancidReportExecController rancidlist report ");
            boolean done = m_inventoryService.runRancidListReport(bean.getDate());
            if (!done){
                log().debug("RancidReportExecController error ");
            }
        } else if (bean.getReporttype().compareTo("inventory") == 0){
            boolean done = m_inventoryService.runNodeBaseInventoryReport(bean.getDate(), bean.getFieldhas());
            if (!done){
                log().debug("RancidReportExecController error ");
            }
        }
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("RancidReportExecController initBinder");
    }
    
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}
