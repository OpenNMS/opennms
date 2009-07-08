/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller.inventory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class AdminRancidCloginUpdateController extends SimpleFormController {
    
    InventoryService m_inventoryService;
    
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
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("AdminRancidCloginIpdateController initBinder");
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
