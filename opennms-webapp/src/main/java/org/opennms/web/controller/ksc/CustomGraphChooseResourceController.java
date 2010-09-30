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
// 2008 Oct 22: Use new loadResourceById when we need an exception if the resource isn't found. - dj@opennms.org
// 2008 Feb 03: Use Assert.state in afterPropertiesSet(). - dj@opennms.org
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
package org.opennms.web.controller.ksc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>CustomGraphChooseResourceController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CustomGraphChooseResourceController extends AbstractController implements InitializingBean {
    private ResourceService m_resourceService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphChooseResource");

        String resourceId = request.getParameter("resourceId");
        if (resourceId == null) {
            throw new MissingParameterException("resourceId");
        }
        
        String selectedResourceId = request.getParameter("selectedResourceId");
        if (selectedResourceId != null) {
            OnmsResource selectedResource = m_resourceService.getResourceById(selectedResourceId);

            Map<String, OnmsResource> selectedResourceAndParents = new HashMap<String, OnmsResource>();
            OnmsResource r = selectedResource;
            while (r != null) {
                selectedResourceAndParents.put(r.getId(), r);
                r = r.getParent();
            }
            
            modelAndView.addObject("selectedResourceAndParents", selectedResourceAndParents);
        }
        
        OnmsResource resource = getResourceService().getResourceById(resourceId);
        modelAndView.addObject("parentResource", resource);
        
        modelAndView.addObject("parentResourcePrefabGraphs", m_resourceService.findPrefabGraphsForResource(resource));

        List<OnmsResource> childResources = getResourceService().findChildResources(resource);
        modelAndView.addObject("resources", childResources);
        
        return modelAndView;
    }

    /**
     * <p>getResourceService</p>
     *
     * @return a {@link org.opennms.web.svclayer.ResourceService} object.
     */
    public ResourceService getResourceService() {
        return m_resourceService;
    }

    /**
     * <p>setResourceService</p>
     *
     * @param resourceService a {@link org.opennms.web.svclayer.ResourceService} object.
     */
    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "property resourceService must be set");
    }

}
