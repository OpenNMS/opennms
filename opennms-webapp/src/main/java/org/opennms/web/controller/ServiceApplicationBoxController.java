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
// Modifications:
//
// 2007 Jul 24: Organize imports. - dj@opennms.org
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

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.web.acegisecurity.Authentication;
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
 * @since 1.6.12
 */
public class ServiceApplicationBoxController extends AbstractController {
    private AdminApplicationService m_adminApplicationService; 

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request);

        List<OnmsApplication> applications = m_adminApplicationService.findByMonitoredService(service.getId());
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/serviceApplication-box", "applications",
                             applications);
        modelAndView.addObject("service", service);
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
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
