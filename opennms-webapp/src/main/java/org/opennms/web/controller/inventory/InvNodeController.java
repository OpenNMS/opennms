package org.opennms.web.controller.inventory;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


public class InvNodeController implements Controller {

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
        String version = request.getParameter("version");
        Map<String, Object> model = m_inventoryService.getInventory(nodeid, 
                                                                         WebSecurityUtils.sanitizeString(group),
                                                                         WebSecurityUtils.sanitizeString(version));
        ModelAndView modelAndView = new ModelAndView("inventory/invnode","model",model);
        return modelAndView;
    }

}
