package org.opennms.web.controller.inventory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>AdminStorageDeleteBucketItemController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AdminStorageDeleteBucketItemController implements Controller {

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

        String bucket = request.getParameter("bucket");
        String filename = request.getParameter("filename");
        if (bucket != null && filename != null && request.isUserInRole(Authentication.ADMIN_ROLE)) {
            boolean done = m_inventoryService.deleteBucketItem(bucket, filename);
            if (!done){
                log().debug("AdminStorageDeleteBucketItemController ModelAndView onSubmit error while deleting status for: "+ bucket);
            }
     }
        Map<String, Object> model  = m_inventoryService.getBuckets(nodeid);
        ModelAndView modelAndView = new ModelAndView("admin/storage/storageAdmin","model",model);
        return modelAndView;
    }
    
    private static Logger log() {
        return Logger.getLogger("Rancid");
    }


}
