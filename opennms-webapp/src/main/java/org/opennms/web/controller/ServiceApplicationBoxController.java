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

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.web.api.Authentication;
import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Service;
import org.opennms.web.svclayer.AdminApplicationService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>ServiceApplicationBoxController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceApplicationBoxController extends AbstractController {
    private AdminApplicationService m_adminApplicationService; 

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request, getServletContext());

        List<OnmsApplication> applications = m_adminApplicationService.findByMonitoredService(service.getId());
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/serviceApplication-box", "applications",
                             applications);
        modelAndView.addObject("service", service);
        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    /**
     * <p>getAdminApplicationService</p>
     *
     * @return a {@link org.opennms.web.svclayer.AdminApplicationService} object.
     */
    public AdminApplicationService getAdminApplicationService() {
        return m_adminApplicationService;
    }

    /**
     * <p>setAdminApplicationService</p>
     *
     * @param adminApplicationService a {@link org.opennms.web.svclayer.AdminApplicationService} object.
     */
    public void setAdminApplicationService(AdminApplicationService adminApplicationService) {
        m_adminApplicationService = adminApplicationService;
    }

}
