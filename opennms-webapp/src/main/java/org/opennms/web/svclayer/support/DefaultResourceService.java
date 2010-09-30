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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.opennms.web.Util;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultResourceService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultResourceService implements ResourceService, InitializingBean {
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;
    private EventProxy m_eventProxy;

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    /**
     * <p>getGraphDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.GraphDao} object.
     */
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceDao != null, "resourceDao property is not set");
        Assert.state(m_graphDao != null, "graphDao property is not set");
        Assert.state(m_eventProxy != null, "eventProxy property is not set");
    }
    
    /**
     * <p>getRrdDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdDirectory() {
        return m_resourceDao.getRrdDirectory();
    }

    /**
     * <p>findDomainResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findDomainResources() {
        return m_resourceDao.findDomainResources();
    }

    /**
     * <p>findNodeResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeResources() {
        return m_resourceDao.findNodeResources();
    }

    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findTopLevelResources() {
        return m_resourceDao.findTopLevelResources();
    }

    /** {@inheritDoc} */
    public List<OnmsResource> findNodeChildResources(int nodeId) {
        List<OnmsResource> resources = new ArrayList<OnmsResource>();
        OnmsResource resource = m_resourceDao.getResourceById(OnmsResource.createResourceId("node", Integer.toString(nodeId)));
        if (resource != null) {
            resources = resource.getChildResources();
            resources.size(); // Get the size to force the list to be loaded
        }
        return resources;
    }

    /** {@inheritDoc} */
    public List<OnmsResource> findDomainChildResources(String domain) {
        List<OnmsResource> resources = new ArrayList<OnmsResource>();
        OnmsResource resource = m_resourceDao.getResourceById(OnmsResource.createResourceId("domain", domain));
        if (resource != null) {
            resources = resource.getChildResources();
            resources.size(); // Get the size to force the list to be loaded
        }
        return resources;
    }
    
    /**
     * <p>findChildResources</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param resourceTypeMatches a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches) {
        List<OnmsResource> matchingChildResources = new LinkedList<OnmsResource>();
        
        if (resource != null) {
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
                    matchingChildResources.add(checkLabelForQuotes(childResource));
                }
            }
        }

        return matchingChildResources;
    }
    
    private static OnmsResource checkLabelForQuotes(OnmsResource childResource) {
        
        String lbl  = Util.convertToJsSafeString(childResource.getLabel());
        
        OnmsResource resource = new OnmsResource(childResource.getName(), lbl, childResource.getResourceType(), childResource.getAttributes());
        resource.setParent(childResource.getParent());
        resource.setEntity(childResource.getEntity());
        resource.setLink(childResource.getLink());
        return resource;
    }

    /** {@inheritDoc} */
    public OnmsResource getResourceById(String id) {
        return m_resourceDao.getResourceById(id);
    }

    /** {@inheritDoc} */
    public List<OnmsResource> getResourceListById(String resourceId) {
        return m_resourceDao.getResourceListById(resourceId);
    }
    
    /** {@inheritDoc} */
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource) {
        return m_graphDao.getPrefabGraphsForResource(resource);
    }
    
    /** {@inheritDoc} */
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
    
    /**
     * <p>promoteGraphAttributesForResource</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     */
    public void promoteGraphAttributesForResource(String resourceId) {
        promoteGraphAttributesForResource(getResourceById(resourceId));
    }
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(DefaultResourceService.class);
    }

    /**
     * <p>findPrefabGraphsForChildResources</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param resourceTypeMatches a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches) {
        Map<String, PrefabGraph> childGraphs = new LinkedHashMap<String, PrefabGraph>();
        for (OnmsResource r : findChildResources(resource, resourceTypeMatches)) {
            for (PrefabGraph g : findPrefabGraphsForResource(r)) {
                childGraphs.put(g.getName(), g);
            }
        }
        return childGraphs.values().toArray(new PrefabGraph[childGraphs.size()]);
    }

    /** {@inheritDoc} */
    public PrefabGraph getPrefabGraph(String name) {
        return m_graphDao.getPrefabGraph(name);
    }

}
