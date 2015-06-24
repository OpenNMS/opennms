/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ksc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(CustomGraphChooseResourceController.class);


    public enum Parameters {
        resourceId,
        selectedResourceId
    }

    private ResourceService m_resourceService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphChooseResource");

        String resourceId = request.getParameter(Parameters.resourceId.toString());
        if (resourceId == null) {
            throw new MissingParameterException(Parameters.resourceId.toString());
        }
        
        String selectedResourceId = request.getParameter(Parameters.selectedResourceId.toString());
        if (selectedResourceId != null) {
            OnmsResource selectedResource = m_resourceService.getResourceById(selectedResourceId);

            Map<String, OnmsResource> selectedResourceAndParents = new HashMap<String, OnmsResource>();
            OnmsResource r = selectedResource;
            while (r != null) {
                selectedResourceAndParents.put(r.getId(), r);
                r = r.getParent();
            }
            
            LOG.debug("handleRequestInternal: addObject {}", selectedResourceAndParents.toString());
            modelAndView.addObject("selectedResourceAndParents", selectedResourceAndParents);
        }
        
        OnmsResource resource = getResourceService().getResourceById(resourceId);
        modelAndView.addObject("parentResource", resource);
        
        modelAndView.addObject("parentResourcePrefabGraphs", m_resourceService.findPrefabGraphsForResource(resource));

        List<OnmsResource> childResources = getResourceService().findChildResources(resource);
        LOG.debug("handleRequestInternal: addObject {}", childResources.toString());
        modelAndView.addObject("resources", childResources);
        
        return modelAndView;
    }

    /**
     * <p>getResourceService</p>
     *
     * @return a {@link org.opennms.web.svclayer.api.ResourceService} object.
     */
    public ResourceService getResourceService() {
        return m_resourceService;
    }

    /**
     * <p>setResourceService</p>
     *
     * @param resourceService a {@link org.opennms.web.svclayer.api.ResourceService} object.
     */
    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "property resourceService must be set");
    }

}
