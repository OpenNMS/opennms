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

package org.opennms.web.svclayer.support;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.svclayer.api.KscReportService;
import org.opennms.web.svclayer.api.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultKscReportService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultKscReportService implements KscReportService, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultKscReportService.class);


    private ResourceService m_resourceService;
    private KSC_PerformanceReportFactory m_kscReportFactory;

    private static final Map<String, String> s_timeSpans = new LinkedHashMap<String, String>();
    private static final Map<String, String> s_timeSpansWithNone = new LinkedHashMap<String, String>();

    /** {@inheritDoc} */
    @Override
    public Report buildDomainReport(String domain) {
        String resourceId = OnmsResource.createResourceId("domain", domain);
        OnmsResource res = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), res, "Domain Report for Domain " + domain);
    }

    /** {@inheritDoc} */
    @Override
    public Report buildNodeReport(int node_id) {
        String resourceId = OnmsResource.createResourceId("node", Integer.toString(node_id));
        OnmsResource node = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), node, "Node Report for Node Number " + node_id);
    }

    /** {@inheritDoc} */
    @Override
    public Report buildNodeSourceReport(String nodeSource) {
        String resourceId = OnmsResource.createResourceId("nodeSource", nodeSource);
        OnmsResource res = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), res, "Node Report for Foreign Source:Id " + nodeSource);
    }

    private static Report buildResourceReport(ResourceService service, OnmsResource parentResource, String title) {
        Report report = new Report();
        report.setTitle(title);
        report.setShow_timespan_button(true);
        report.setShow_graphtype_button(true);

        List<OnmsResource> resources = service.findChildResources(parentResource, "interfaceSnmp");
        for (OnmsResource resource : resources) {
            PrefabGraph[] graphs = service.findPrefabGraphsForResource(resource);
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

    private static String getResourceIdForGraph(Graph graph) {
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
            } else if (graph.getNodeSource() != null && !graph.getNodeSource().equals("null")) {
                parentResourceTypeName = "nodeSource";
                parentResourceName = graph.getNodeSource();
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

        return resourceId;
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getResourceFromGraph(Graph graph) {
        return getResourceService().getResourceById(getResourceIdForGraph(graph));
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesFromGraphs(List<Graph> graphs) {
        Assert.notNull(graphs, "graph argument cannot be null");
        List<OnmsResource> resources = new LinkedList<OnmsResource>();
        HashMap<String, List<OnmsResource>> resourcesMap = new HashMap<String, List<OnmsResource>>();
        for(Graph graph : graphs) {
            String resourceId = getResourceIdForGraph(graph);

            if (resourceId != null) {
                String[] resourceParts = DefaultGraphResultsService.parseResourceId(resourceId);
                if (resourceParts == null) {
                    LOG.warn("getResourcesFromGraphs: unparsable resourceId, skipping: {}", resourceId);
                    continue;
                }

                String parent = resourceParts[0];
                String childType = resourceParts[1];
                String childName = resourceParts[2];

                List<OnmsResource> resourcesForParent = resourcesMap.get(parent);
                if (resourcesForParent == null) {
                    try {
                        resourcesForParent = getResourceService().getResourceListById(resourceId);
                        if (resourcesForParent == null) {
                            LOG.warn("getResourcesFromGraphs: no resources found for parent {}", parent);
                            continue;
                        } else {
                            resourcesMap.put(parent, resourcesForParent);
                            LOG.debug("getResourcesFromGraphs: add resourceList to map for {}", parent);
                        }
                    } catch (Throwable e) {
                        LOG.warn("getResourcesFromGraphs: unexpected exception thrown while fetching resource list for \"{}\", skipping resource", parent, e);
                        continue;
                    }
                }

                for (OnmsResource r : resourcesForParent) {
                    if (childType.equals(r.getResourceType().getName()) && childName.equals(r.getName())) {
                        resources.add(r);
                        LOG.debug("getResourcesFromGraphs: found resource in map{}", r.toString());
                        break;
                    }
                }
            }
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

    /** {@inheritDoc} */
    @Override
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
     * @return a {@link java.util.Map} object.
     */
    @Override
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
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "resourceService property has not been set");
        Assert.state(m_kscReportFactory != null, "kscReportFactory property has not been set");

        initTimeSpans();
    }

    /**
     * <p>getReportMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<Integer, Report> getReportMap() {
        return m_kscReportFactory.getReportMap();
    }


}
