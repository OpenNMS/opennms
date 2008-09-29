//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.ChooseResourceService;
import org.opennms.web.svclayer.support.ChooseResourceModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class ChooseResourceController extends AbstractController implements InitializingBean {
    private ChooseResourceService m_chooseResourceService;
    private String m_defaultEndUrl;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "parentResourceId or", "parentResourceType and parentResource" };

        String endUrl = WebSecurityUtils.sanitizeString(request.getParameter("endUrl"));

        String resourceId = WebSecurityUtils.sanitizeString(request.getParameter("parentResourceId"));
        if (resourceId == null) {
            String resourceType = WebSecurityUtils.sanitizeString(request.getParameter("parentResourceType"));
            String resource = WebSecurityUtils.sanitizeString(request.getParameter("parentResource"));
            if (request.getParameter("parentResourceType") == null) {
                throw new MissingParameterException("parentResourceType", requiredParameters);
            }
            if (req.getParameter("parentResource") == null) {
                throw new MissingParameterException("parentResource", requiredParameters);
            }
            
            resourceId = OnmsResource.createResourceId(resourceType, resource);
        }
        
        if (endUrl == null || "".equals(endUrl)) {
            endUrl = m_defaultEndUrl;
        }

        ChooseResourceModel model = 
            m_chooseResourceService.findChildResources(resourceId,
                                                       endUrl);
        
        return new ModelAndView("/graph/chooseresource",
                                "model",
                                model);
    }
    
    public void afterPropertiesSet() {
        if (m_chooseResourceService == null) {
            throw new IllegalStateException("chooseResourceService property not set");
        }
        
        if (m_defaultEndUrl == null) {
            throw new IllegalStateException("defaultEndUrl property not set");
        }
    }

    public ChooseResourceService getChooseResourceService() {
        return m_chooseResourceService;
    }

    public void setChooseResourceService(
            ChooseResourceService chooseResourceService) {
        m_chooseResourceService = chooseResourceService;
    }

    public String getDefaultEndUrl() {
        return m_defaultEndUrl;
    }

    public void setDefaultEndUrl(String defaultEndUrl) {
        m_defaultEndUrl = defaultEndUrl;
    }
}
