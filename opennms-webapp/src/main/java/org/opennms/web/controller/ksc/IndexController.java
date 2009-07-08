/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.controller.ksc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.KscReportService;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class IndexController extends AbstractController implements InitializingBean {
    
    private ResourceService m_resourceService;
    private KscReportService m_kscReportService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("KSC/index");

        modelAndView.addObject("kscReadOnly", (request.isUserInRole(Authentication.READONLY_ROLE)) || (request.getRemoteUser() == null));
        modelAndView.addObject("reports", getKscReportService().getReportList());
        modelAndView.addObject("nodeResources", getResourceService().findNodeResources());
        modelAndView.addObject("domainResources", getResourceService().findDomainResources());
        
        return modelAndView;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }
    
    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "property resourceService must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }
}
