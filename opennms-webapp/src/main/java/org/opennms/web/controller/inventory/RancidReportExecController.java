package org.opennms.web.controller.inventory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.report.configuration.svclayer.ConfigurationReportCriteria;
import org.opennms.report.configuration.svclayer.ConfigurationReportService;
import org.opennms.report.inventory.svclayer.InventoryReportCriteria;
import org.opennms.report.inventory.svclayer.InventoryReportService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class RancidReportExecController extends SimpleFormController {
    
//    InventoryService m_inventoryService;
    ConfigurationReportService m_configurationReportService;
    InventoryReportService m_inventoryReportService;
    
    
    
    public ConfigurationReportService getConfigurationReportService() {
        return m_configurationReportService;
    }
    public void setConfigurationReportService(
            ConfigurationReportService configurationReportService) {
        m_configurationReportService = configurationReportService;
    }
    public InventoryReportService getInventoryReportService() {
        return m_inventoryReportService;
    }
    public void setInventoryReportService(
            InventoryReportService inventoryReportService) {
        m_inventoryReportService = inventoryReportService;
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("RancidReportExecController ModelAndView onSubmit");
        
        RancidReportExecCommClass bean = (RancidReportExecCommClass) command;
        
        log().debug("RancidReportExecController ModelAndView type" + bean.getReporttype());
        log().debug("RancidReportExecController ModelAndView type" + bean.getFieldhas());
        
        String user = request.getRemoteUser();
        Date reportRequestDate = new Date();

        
        ModelAndView mav = new ModelAndView(getSuccessView());

        if (bean.getReporttype().compareTo("rancidlist") == 0){
            log().debug("RancidReportExecController rancidlist report ");
            ConfigurationReportCriteria criteria = new ConfigurationReportCriteria(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
//            boolean done = m_inventoryService.runRancidListReport(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
            boolean done = m_configurationReportService.runReport(criteria);
            mav.addObject("type", "Rancid List");
            if (!done){
                log().debug("RancidReportExecController error ");
            }
        } else if (bean.getReporttype().compareTo("inventory") == 0){
            log().debug("RancidReportExecController inventory report ");
            InventoryReportCriteria criteria = new InventoryReportCriteria(bean.getDate(), bean.getFieldhas(), bean.getReportfiletype(),bean.getReportemail(), user, reportRequestDate);
            boolean done = m_inventoryReportService.runReport(criteria);
            mav.addObject("type", "Inventory Report");
            if (!done){
                log().debug("RancidReportExecController error ");
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
        try {
            mav.addObject("date", format.parse(bean.getDate()));
        }
        catch (ParseException pe){
            mav.addObject("date", format.format(Calendar.getInstance().getTime()));
        }
        mav.addObject("searchfield", bean.getFieldhas());
        if( bean.getReportfiletype().compareTo("pdftype") == 0){
            mav.addObject("reportformat", "PDF");
        } else {
            mav.addObject("reportformat", "HTML");
        }
        
        return mav;

        
//        String redirectURL = request.getHeader("Referer");
//        response.sendRedirect(redirectURL);
//        return super.onSubmit(request, response, command, errors);
    }
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("RancidReportExecController initBinder");
    }
    
   
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}
