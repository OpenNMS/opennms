package org.opennms.web.controller.inventory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class RancidViewVcController implements Controller {

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
        String iframelink = request.getParameter("viewvc");
        String group = request.getParameter("group");
        int nodeid = WebSecurityUtils.safeParseInt(node);
        Map<String, Object> model = m_inventoryService.getRancidNodeBase(nodeid);
        model.put("iframelink", iframelink);
        model.put("group", group);
        ModelAndView modelAndView = new ModelAndView("inventory/rancidViewVc","model",model);
        return modelAndView;
    }

}
