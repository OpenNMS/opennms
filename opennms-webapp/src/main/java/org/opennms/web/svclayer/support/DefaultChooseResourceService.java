
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.web.api.Util;
import org.opennms.web.svclayer.ChooseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>DefaultChooseResourceService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultChooseResourceService implements ChooseResourceService, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultChooseResourceService.class);


    public ResourceDao m_resourceDao;

    /** {@inheritDoc} */
    @Override
    public ChooseResourceModel findChildResources(String resourceId, String endUrl) {
        if (resourceId == null) {
            throw new IllegalArgumentException("resourceId parameter may not be null");
        }
        
        if (endUrl == null) {
            throw new IllegalArgumentException("endUrl parameter may not be null");
        }

        ChooseResourceModel model = new ChooseResourceModel();
        model.setEndUrl(endUrl);
        
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("resource \"" + resourceId + "\" could not be found");
        }

        model.setResource(resource);
        Map<OnmsResourceType, List<OnmsResource>> resourceTypeMap = new LinkedHashMap<OnmsResourceType, List<OnmsResource>>();
        
       
        for (OnmsResource childResource : resource.getChildResources()) {
            if (!resourceTypeMap.containsKey(childResource.getResourceType())) {
                resourceTypeMap.put(childResource.getResourceType(), new LinkedList<OnmsResource>());
            }
            // See bug 3760: These values have been known to contain a % sign so they are 
            // not safe to pass to LogUtils.infof()
            // http://bugzilla.opennms.org/show_bug.cgi?id=3760
                LOG.info("getId(): {}", childResource.getId());
                LOG.info("getName(): {}", childResource.getName());
            //checkLabelForQuotes(
            resourceTypeMap.get(childResource.getResourceType()).add(checkLabelForQuotes(childResource));
        }
        
        model.setResourceTypes(resourceTypeMap);

        return model;
    }
    
    private OnmsResource checkLabelForQuotes(OnmsResource childResource) {
        
        String lbl  = Util.convertToJsSafeString(childResource.getLabel());
        
        OnmsResource resource = new OnmsResource(childResource.getName(), lbl, childResource.getResourceType(), childResource.getAttributes());
        resource.setParent(childResource.getParent());
        resource.setEntity(childResource.getEntity());
        resource.setLink(childResource.getLink());
        return resource;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        if (m_resourceDao == null) {
            throw new IllegalStateException("resourceDao property not set");
        }
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

}
