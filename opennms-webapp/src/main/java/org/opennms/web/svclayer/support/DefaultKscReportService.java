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
 * 2009 Jan 26: added getResourcesFromGraphs - part of ksc performance improvement. - ayres@opennms.org
 * 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
 * 2008 Oct 19: Bug #2823: Fix for NullPointerException if a graph doesn't have
 *              a resourceId, nodeId, or domain. - dj@opennms.org
 * 
 * Created: January 2, 2007
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.svclayer.KscReportService;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultKscReportService implements KscReportService, InitializingBean {
    
    private ResourceService m_resourceService;
    private KSC_PerformanceReportFactory m_kscReportFactory;

    private static final LinkedHashMap<String, String> s_timeSpans = new LinkedHashMap<String, String>();
    private static final LinkedHashMap<String, String> s_timeSpansWithNone = new LinkedHashMap<String, String>();

    public Report buildDomainReport(String domain) {
        String resourceId = OnmsResource.createResourceId("domain", domain);
        OnmsResource node = getResourceService().loadResourceById(resourceId);
        return buildResourceReport(node, "Domain Report for Domain " + domain);
    }

    public Report buildNodeReport(int node_id) {
        String resourceId = OnmsResource.createResourceId("node", Integer.toString(node_id));
        OnmsResource node = getResourceService().loadResourceById(resourceId);
        return buildResourceReport(node, "Node Report for Node Number " + node_id);
    }
    
    private Report buildResourceReport(OnmsResource parentResource, String title) {
        Report report = new Report();
        report.setTitle(title);
        report.setShow_timespan_button(true);
        report.setShow_graphtype_button(true);

        List<OnmsResource> resources = getResourceService().findChildResources(parentResource, "interfaceSnmp");
        for (OnmsResource resource : resources) {
            PrefabGraph[] graphs = getResourceService().findPrefabGraphsForResource(resource);
            if (graphs.length == 0) {
                continue;
            }
            
            Graph graph = new Graph();
            graph.setTitle("");
            graph.setResourceId(resource.getId());
            graph.setTimespan("7_day");
            graph.setGraphtype(graphs[0].getName());
            
            report.addGraph(graph);
        }
        return report;
    }


    public OnmsResource getResourceFromGraph(Graph graph) {
        Assert.notNull(graph, "graph argument cannot be null");
        
        String resourceId;
        if (graph.getResourceId() != null) {
            resourceId = graph.getResourceId();
        } else {
            String parentResourceTypeName;
            String parentResourceName;
            String resourceTypeName;
            String resourceName;
            
            if (graph.getNodeId() != null && !graph.getNodeId().equals("null")) {
                parentResourceTypeName = "node";
                parentResourceName = graph.getNodeId();
            } else if (graph.getDomain() != null && !graph.getDomain().equals("null")) {
                parentResourceTypeName = "domain";
                parentResourceName = graph.getDomain();
            } else {
                throw new IllegalArgumentException("Graph does not have a resourceId, nodeId, or domain.");
            }
            
            String intf = graph.getInterfaceId();
            if (intf == null || "".equals(intf)) {
                resourceTypeName = "nodeSnmp";
                resourceName = "";
            } else {
                resourceTypeName = "interfaceSnmp";
                resourceName = intf;
            }
    
            resourceId = OnmsResource.createResourceId(parentResourceTypeName, parentResourceName, resourceTypeName, resourceName);
        }
        
        return getResourceService().loadResourceById(resourceId);
    }
    
    public List<OnmsResource> getResourcesFromGraphs(List<Graph> graphs) {
        Assert.notNull(graphs, "graph argument cannot be null");
        List<OnmsResource> resources = new LinkedList<OnmsResource>();
        HashMap<String, List<OnmsResource>> resourcesMap = new HashMap<String, List<OnmsResource>>();
        for(Graph graph : graphs) {
            String resourceId;
            if (graph.getResourceId() != null) {
                resourceId = graph.getResourceId();
            } else {
                String parentResourceTypeName;
                String parentResourceName;
                String resourceTypeName;
                String resourceName;
            
                if (graph.getNodeId() != null && !graph.getNodeId().equals("null")) {
                    parentResourceTypeName = "node";
                    parentResourceName = graph.getNodeId();
                } else if (graph.getDomain() != null && !graph.getDomain().equals("null")) {
                    parentResourceTypeName = "domain";
                    parentResourceName = graph.getDomain();
                } else {
                    throw new IllegalArgumentException("Graph does not have a resourceId, nodeId, or domain.");
                }
            
                String intf = graph.getInterfaceId();
                if (intf == null || "".equals(intf)) {
                    resourceTypeName = "nodeSnmp";
                    resourceName = "";
                } else {
                    resourceTypeName = "interfaceSnmp";
                    resourceName = intf;
                }
                resourceId = OnmsResource.createResourceId(parentResourceTypeName, parentResourceName, resourceTypeName, resourceName);
            }
            
            String parent = resourceId.substring(0, resourceId.indexOf("]") + 1);
            String child = resourceId.substring(resourceId.indexOf("]") + 2);
            String childType = child.substring(0, child.indexOf("["));
            String childName = child.substring(child.indexOf("[") + 1, child.indexOf("]"));
            OnmsResource resource = null;
            if (resourceId != null) {
                if (!resourcesMap.containsKey(parent)) {
                    List<OnmsResource> resourceList = getResourceService().getResourceListById(resourceId);
                    resourcesMap.put(parent, resourceList);
                    log().debug("getResourcesFromGraphs: add resourceList to map for " + parent);
                }
            
                for (OnmsResource r : resourcesMap.get(parent)) {
                    if (childType.equals(r.getResourceType().getName())
                            && childName.equals(r.getName())) {
                        resource = r;
                        log().debug("getResourcesFromGraphs: found resource in map" + r.toString());
                        break;
                    }
                }
            }
            resources.add(resource);
        }
        return resources;
    }

    
    private void initTimeSpans() {
        for (String timeSpan : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
            s_timeSpans.put(timeSpan, timeSpan);
        }
        
        s_timeSpansWithNone.put("none", "none");
        s_timeSpansWithNone.putAll(s_timeSpans);
    }

    public Map<String, String> getTimeSpans(boolean includeNone) {
        if (includeNone) {
            return s_timeSpansWithNone;
        } else {
            return s_timeSpans;
        }
    }
    
    public Map<Integer, String> getReportList() {
        return m_kscReportFactory.getReportList();  
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "resourceService property has not been set");
        Assert.state(m_kscReportFactory != null, "kscReportFactory property has not been set");
        
        initTimeSpans();
    }
    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

}
