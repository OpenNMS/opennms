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
        Map<String, Object> model  = m_inventoryService.getBuckets(nodeid);
        ModelAndView modelAndView = new ModelAndView("admin/storage/storageAdmin","model",model);
        return modelAndView;
    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }


}
