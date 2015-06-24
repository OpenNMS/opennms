/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.inventory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.api.Authentication;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * <p>AdminRancidDeleteController class.</p>
 */
@Controller
@RequestMapping("/admin/rancid/rancidDelete.htm")
public class AdminRancidDeleteController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminRancidDeleteController.class);

    @Autowired
    private InventoryService m_inventoryService;

    @RequestMapping(method=RequestMethod.POST)
    public String onSubmit(HttpServletRequest request, HttpServletResponse response, AdminRancidRouterDbCommClass bean) {

        LOG.debug("AdminRancidDeleteController ModelAndView onSubmit");

        LOG.debug("AdminRancidDeleteController ModelAndView onSubmit delete device[{}] group[{}] status[{}]", bean.getDeviceName(), bean.getGroupName(), bean.getStatusName());

        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
            boolean done = m_inventoryService.deleteNodeOnRouterDb(bean.getGroupName(), bean.getDeviceName());
            if (!done){
                LOG.debug("AdminRancidDeleteController ModelAndView onSubmit error while deleting status for{}/{}", bean.getGroupName(),  bean.getDeviceName());
            }
        }

        return "admin/rancid/rancidAdmin";
    }
}
