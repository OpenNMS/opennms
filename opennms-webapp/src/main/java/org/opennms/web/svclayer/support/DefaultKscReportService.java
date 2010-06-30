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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * <p>DefaultKscReportService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DefaultKscReportService implements KscReportService, InitializingBean {
    
    private ResourceService m_resourceService;
    private KSC_PerformanceReportFactory m_kscReportFactory;

    private static final LinkedHashMap<String, String> s_timeSpans = new LinkedHashMap<String, String>();
    private static final LinkedHashMap<String, String> s_timeSpansWithNone = new LinkedHashMap<String, String>();

    /** {@inheritDoc} */
    public Report buildDomainReport(String domain) {
        String resourceId = OnmsResource.createResourceId("domain", domain);
        OnmsResource node = getResourceService().getResourceById(resourceId, true);
        return buildResourceReport(node, "Domain Report for Domain " + domain);
    }

    /** {@inheritDoc} */
    public Report buildNodeReport(int node_id) {
        String resourceId = OnmsResource.createResourceId("node", Integer.toString(node_id));
        OnmsResource node = getResourceService().getResourceById(resourceId, true);
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


    /** {@inheritDoc} */
    public OnmsResource getResourceFromGraph(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph argument cannot be null");
        }
        
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
            } else {
                parentResourceTypeName = "domain";
                parentResourceName = graph.getDomain();
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
        
        return getResourceService().getResourceById(resourceId, true);
    }
    

    private void initTimeSpans() {
        for (String timeSpan : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
            s_timeSpans.put(timeSpan, timeSpan);
        }
        
        s_timeSpansWithNone.put("none", "none");
        s_timeSpansWithNone.putAll(s_timeSpans);
    }

    /** {@inheritDoc} */
    public Map<String, String> getTimeSpans(boolean includeNone) {
        if (includeNone) {
            return s_timeSpansWithNone;
        } else {
            return s_timeSpans;
        }
    }
    
    /**
     * <p>getReportList</p>
     *
     * @return a java$util$Map object.
     */
    public Map<Integer, String> getReportList() {
        return m_kscReportFactory.getReportList();  
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
     * <p>getKscReportFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    /**
     * <p>setKscReportFactory</p>
     *
     * @param kscReportFactory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "resourceService property has not been set");
        Assert.state(m_kscReportFactory != null, "kscReportFactory property has not been set");
        
        initTimeSpans();
    }

}
