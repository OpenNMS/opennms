package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidNodeAuthentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class AdminRancidCloginController extends SimpleFormController {
    
    InventoryService m_inventoryService;
    
    RWSConfig m_rwsConfig;

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("AdminRancidCloginController ModelAndView onSubmit");
        
        AdminRancidCloginCommClass bean = (AdminRancidCloginCommClass) command;
        
        log().debug("AdminRancidCloginController ModelAndView onSubmit following changes"+
                    "userID ["+ bean.getUserID() +"] "+
                    "pass [" + bean.getPass() +"] "+
                    "enpass [" + bean.getEnpass()+"] "+
                    "loginM [" + bean.getLoginM()+"] "+
                    "autoE [" + bean.getAutoE()+"] "+
                    "groupName [" + bean.getGroupName()+"] "+
                    "deviceName [" + bean.getDeviceName() + "] "); 

        ConnectionProperties cp = new ConnectionProperties(m_rwsConfig.getBaseUrl().getServer_url(),m_rwsConfig.getBaseUrl().getDirectory(),m_rwsConfig.getBaseUrl().getTimeout());

        RancidNodeAuthentication rna = RWSClientApi.getRWSAuthNode(cp, bean.getDeviceName());
        rna.setUser(bean.getUserID());
        rna.setPassword(bean.getPass());
        rna.setConnectionMethod(bean.getLoginM());
        rna.setEnablePass(bean.getAutoE());
        boolean autoe = false;
        if (bean.getAutoE().compareTo("1")==0) {
            autoe = true;
        }
        rna.setAutoEnable(autoe);
        RWSClientApi.createOrUpdateRWSAuthNode(cp,rna);
        log().debug("AdminRancidCloginController ModelAndView onSubmit changes submitted");


        
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("AdminRancidCloginController initBinder");
    }

    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }
    public void setRwsConfig(RWSConfig rwsConfig) {
        log().debug("AdminRancidCloginController setRwsConfig");
        m_rwsConfig = rwsConfig;
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
