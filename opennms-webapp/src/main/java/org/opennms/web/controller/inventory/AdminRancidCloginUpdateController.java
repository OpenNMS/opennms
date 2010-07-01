package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * <p>AdminRancidCloginUpdateController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AdminRancidCloginUpdateController extends SimpleFormController {
    
    InventoryService m_inventoryService;
    
    /** {@inheritDoc} */
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("AdminRancidCloginUpdateController ModelAndView onSubmit");
        
        AdminRancidCloginCommClass bean = (AdminRancidCloginCommClass) command;
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {

            boolean done = m_inventoryService.updateClogin(bean.getDeviceName(), bean.getGroupName(), bean.getUserID(), bean.getPass(),
                                            bean.getEnpass(), bean.getLoginM(), bean.getAutoE());
            if (!done){
                log().debug("AdminRancidCloginUpdateController error on submitting cLogin changes");
            }
        }
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }
    /** {@inheritDoc} */
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("AdminRancidCloginIpdateController initBinder");
    }
    
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

    
    private static Logger log() {
        return Logger.getLogger("Rancid");
    }
}
