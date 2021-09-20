/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.api.Authentication;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>NodeCategoryBoxController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeCategoryBoxController extends AbstractController {
    private AdminCategoryService m_adminCategoryService; 

    /** {@inheritDoc} */
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
        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    /**
     * <p>getAdminCategoryService</p>
     *
     * @return a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    /**
     * <p>setAdminCategoryService</p>
     *
     * @param adminCategoryService a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public void setAdminCategoryService(AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

}
