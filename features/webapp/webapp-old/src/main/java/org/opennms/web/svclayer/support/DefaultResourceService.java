/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Jan 26: added getResourceListById - part of ksc performance improvement. - ayres@opennms.org
 * 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
 * 2007 Aug 02: Add findTopLevelResources(). - dj@opennms.org
 *              (merged to trunk by ayersw on 2007 Aug 17)
 * 2007 May 12: Add getRrdDirectory(), update afterPropertiesSet(). - dj@opennms.org
 * 
 * Created: January 2, 2007
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultResourceService implements ResourceService, InitializingBean {
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;
    private EventProxy m_eventProxy;

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }
    
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceDao != null, "resourceDao property is not set");
        Assert.state(m_graphDao != null, "graphDao property is not set");
        Assert.state(m_eventProxy != null, "eventProxy property is not set");
    }
    
    public File getRrdDirectory() {
        return m_resourceDao.getRrdDirectory();
    }

    public List<OnmsResource> findDomainResources() {
        return m_resourceDao.findDomainResources();
    }

    public List<OnmsResource> findNodeResources() {
        return m_resourceDao.findNodeResources();
    }

    public List<OnmsResource> findTopLevelResources() {
        return m_resourceDao.findTopLevelResources();
    }

    public List<OnmsResource> findNodeChildResources(int nodeId) {
        OnmsResource resource = m_resourceDao.loadResourceById(OnmsResource.createResourceId("node", Integer.toString(nodeId)));
        List<OnmsResource> resources = resource.getChildResources();
        resources.size(); // Get the size to force the list to be loaded
        return resources;
    }

    public List<OnmsResource> findDomainChildResources(String domain) {
        OnmsResource resource = m_resourceDao.loadResourceById(OnmsResource.createResourceId("domain", domain));
        List<OnmsResource> resources = resource.getChildResources();
        resources.size(); // Get the size to force the list to be loaded
        return resources;
    }
    
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches) {
        List<OnmsResource> matchingChildResources = new LinkedList<OnmsResource>();
        
        for (OnmsResource childResource : resource.getChildResources()) {
            boolean addGraph = false;
            if (resourceTypeMatches.length > 0) {
                for (String resourceTypeMatch : resourceTypeMatches) {
                    if (resourceTypeMatch.equals(childResource.getResourceType().getName())) {
                        addGraph = true;
                        break;
                    }
                }
            } else {
                addGraph = true;
            }
        
            if (addGraph) {
                matchingChildResources.add(childResource);
            }
        }

        return matchingChildResources;
    }

    public OnmsResource getResourceById(String id) {
        return m_resourceDao.getResourceById(id);
    }

    public OnmsResource loadResourceById(String id) {
        return m_resourceDao.loadResourceById(id);
    }
    
    public List<OnmsResource> getResourceListById(String resourceId) {
        return m_resourceDao.getResourceListById(resourceId);
    }
    
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource) {
        return m_graphDao.getPrefabGraphsForResource(resource);
    }
    
    public void promoteGraphAttributesForResource(OnmsResource resource) {
        String baseDir = getRrdDirectory().getAbsolutePath();
        List<String> rrdFiles = new LinkedList<String>();
        for(RrdGraphAttribute attribute : resource.getRrdGraphAttributes().values()) {
            rrdFiles.add(baseDir + File.separator + attribute.getRrdRelativePath());
        }
        EventBuilder bldr = new EventBuilder(EventConstants.PROMOTE_QUEUE_DATA_UEI, "OpenNMS.Webapp");
        bldr.addParam(EventConstants.PARM_FILES_TO_PROMOTE, rrdFiles);
        
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            log().warn("Unable to send file promotion event to opennms: " + e, e);
        }
    }
    
    public void promoteGraphAttributesForResource(String resourceId) {
        promoteGraphAttributesForResource(loadResourceById(resourceId));
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches) {
        Map<String, PrefabGraph> childGraphs = new LinkedHashMap<String, PrefabGraph>();
        for (OnmsResource r : findChildResources(resource, resourceTypeMatches)) {
            for (PrefabGraph g : findPrefabGraphsForResource(r)) {
                childGraphs.put(g.getName(), g);
            }
        }
        return childGraphs.values().toArray(new PrefabGraph[childGraphs.size()]);
    }

    public PrefabGraph getPrefabGraph(String name) {
        return m_graphDao.getPrefabGraph(name);
    }

}
