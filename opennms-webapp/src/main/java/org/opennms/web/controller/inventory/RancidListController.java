package org.opennms.web.controller.inventory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


public class RancidListController implements Controller {

    InventoryService m_inventoryService;
    
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse arg1) throws Exception {
       
        String node = request.getParameter("node");
        int nodeid = WebSecurityUtils.safeParseInt(node);
        String group = request.getParameter("groupname");
        Map<String, Object> model;
        if (WebSecurityUtils.sanitizeString(group).compareTo("*") == 0){
            model = m_inventoryService.getRancidNodeList(nodeid);
        }
        else {
            model = m_inventoryService.getRancidNodeList(nodeid,WebSecurityUtils.sanitizeString(group));
        }

        ModelAndView modelAndView = new ModelAndView("inventory/rancidList","model",model);
        return modelAndView;
    }

}
