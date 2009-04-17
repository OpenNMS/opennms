package org.opennms.web.controller.inventory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


public class AdminStorageDeleteBucketItemController implements Controller {

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

        String bucket = request.getParameter("bucket");
        String filename = request.getParameter("filename");
        if (bucket != null && filename != null && request.isUserInRole(Authentication.ADMIN_ROLE)) {
            boolean done = m_inventoryService.deleteBucketItem(bucket, filename);
            if (!done){
                log().debug("AdminStorageDeleteBucketItemController ModelAndView onSubmit error while deleting status for: "+ bucket);
            }
     }
        Map<String, Object> model  = m_inventoryService.getBuckets(nodeid,request.isUserInRole(Authentication.ADMIN_ROLE));
        ModelAndView modelAndView = new ModelAndView("admin/storage/storageAdmin","model",model);
        return modelAndView;
    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }


}
