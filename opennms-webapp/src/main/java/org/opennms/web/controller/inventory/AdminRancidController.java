package org.opennms.web.controller.inventory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>AdminRancidController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AdminRancidController implements Controller {

    InventoryService m_inventoryService;
    
    /**
     * <p>getInventoryService</p>
     *
     * @return a {@link org.opennms.web.svclayer.inventory.InventoryService} object.
     */
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    /**
     * <p>setInventoryService</p>
     *
     * @param inventoryService a {@link org.opennms.web.svclayer.inventory.InventoryService} object.
     */
    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    /** {@inheritDoc} */
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse arg1) throws Exception {
            
        

        String node = request.getParameter("node");
        int nodeid = WebSecurityUtils.safeParseInt(node);
        String group = request.getParameter("group");
        Map<String, Object> model;
        if (group != null) {
            model   = m_inventoryService.getRancidNodeWithCLoginForGroup(nodeid,WebSecurityUtils.sanitizeString(group),request.isUserInRole(Authentication.ADMIN_ROLE));
        } else {
            model   = m_inventoryService.getRancidNodeWithCLogin(nodeid,request.isUserInRole(Authentication.ADMIN_ROLE));            
        }
        ModelAndView modelAndView = new ModelAndView("admin/rancid/rancidAdmin","model",model);
        return modelAndView;
    }

}
