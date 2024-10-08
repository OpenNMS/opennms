/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.support;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourceId;
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
        ResourceId resourceId = ResourceId.get("domain", domain);
        OnmsResource res = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), res, "Domain Report for Domain " + domain);
    }

    /** {@inheritDoc} */
    @Override
    public Report buildNodeReport(int node_id) {
        ResourceId resourceId = ResourceId.get("node", Integer.toString(node_id));
        OnmsResource node = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), node, "Node Report for Node Number " + node_id);
    }

    /** {@inheritDoc} */
    @Override
    public Report buildNodeSourceReport(String nodeSource) {
        ResourceId resourceId = ResourceId.get("nodeSource", nodeSource);
        OnmsResource res = getResourceService().getResourceById(resourceId);
        return buildResourceReport(getResourceService(), res, "Node Report for Foreign Source:Id " + nodeSource);
    }

    private static Report buildResourceReport(ResourceService service, OnmsResource parentResource, String title) {
        Report report = new Report();
        report.setTitle(title);
        report.setShowTimespanButton(true);
        report.setShowGraphtypeButton(true);

        List<OnmsResource> resources = service.findChildResources(parentResource, "interfaceSnmp");
        for (OnmsResource resource : resources) {
            PrefabGraph[] graphs = service.findPrefabGraphsForResource(resource);
            if (graphs.length == 0) {
                continue;
            }

            Graph graph = new Graph();
            graph.setTitle("");
            graph.setResourceId(resource.getId().toString());
            graph.setTimespan("7_day");
            graph.setGraphtype(graphs[0].getName());

            report.addGraph(graph);
        }
        return report;
    }

    private static ResourceId getResourceIdForGraph(Graph graph)  {
        Assert.notNull(graph, "graph argument cannot be null");

        ResourceId resourceId = null;
        if (graph.getResourceId().isPresent()) {
            // Legacy code has encoded resourceId, decode always as there is no easy to way to determine if it is encoded string.
            // If resourceId is not an encoded one, decode will always yield the original.
            // See issue NMS-10309
            try {
                String decodedResourceId = URLDecoder.decode(graph.getResourceId().get(), StandardCharsets.UTF_8.name());
                resourceId = ResourceId.fromString(decodedResourceId);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error while decoding resourceId", e);
            }
        } else {
            String parentResourceTypeName;
            String parentResourceName;
            String resourceTypeName;
            String resourceName;

            if (graph.getNodeId().isPresent() && !graph.getNodeId().get().equals("null")) {
                parentResourceTypeName = "node";
                parentResourceName = graph.getNodeId().get();
            } else if (graph.getNodeSource().isPresent() && !graph.getNodeSource().get().equals("null")) {
                parentResourceTypeName = "nodeSource";
                parentResourceName = graph.getNodeSource().get();
            } else if (graph.getDomain().isPresent() && !graph.getDomain().get().equals("null")) {
                parentResourceTypeName = "domain";
                parentResourceName = graph.getDomain().get();
            } else {
                throw new IllegalArgumentException("Graph does not have a resourceId, nodeId, or domain.");
            }

            String intf = graph.getInterfaceId().orElse(null);
            if (intf == null || "".equals(intf)) {
                resourceTypeName = "nodeSnmp";
                resourceName = "";
            } else {
                resourceTypeName = "interfaceSnmp";
                resourceName = intf;
            }

            resourceId = ResourceId.get(parentResourceTypeName, parentResourceName).resolve(resourceTypeName, resourceName);
        }

        return resourceId;
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getResourceFromGraph(Graph graph) {
        return getResourceService().getResourceById(getResourceIdForGraph(graph));
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
