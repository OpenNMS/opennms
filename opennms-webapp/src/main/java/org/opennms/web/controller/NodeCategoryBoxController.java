//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class NodeCategoryBoxController extends AbstractController {
    private AdminCategoryService m_adminCategoryService; 

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nodeIdString = request.getParameter("node");

        if (nodeIdString == null) {
            throw new MissingParameterException("node");
        }

        int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        List<OnmsCategory> categories = m_adminCategoryService.findByNode(nodeId);
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/nodeCategory-box", "categories",
                             categories);
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    public void setAdminCategoryService(AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

}
