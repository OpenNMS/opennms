/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@SuppressWarnings("deprecation")
public class AdminRancidCloginUpdateController extends SimpleFormController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdminRancidCloginUpdateController.class);

    
    InventoryService m_inventoryService;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        LOG.debug("AdminRancidCloginUpdateController ModelAndView onSubmit");
        
        AdminRancidCloginCommClass bean = (AdminRancidCloginCommClass) command;
        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {

            boolean done = m_inventoryService.updateClogin(bean.getDeviceName(), bean.getGroupName(), bean.getUserID(), bean.getPass(),
                                            bean.getEnpass(), bean.getLoginM(), bean.getAutoE());
            if (!done){
                LOG.debug("AdminRancidCloginUpdateController error on submitting cLogin changes");
            }
        }
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
        return super.onSubmit(request, response, command, errors);
    }
    /** {@inheritDoc} */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        LOG.debug("AdminRancidCloginIpdateController initBinder");
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

}
