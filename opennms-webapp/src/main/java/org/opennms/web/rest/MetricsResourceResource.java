/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.web.rest;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@PerRequest
@Scope("prototype")
@Path("resource")
@Transactional
public class MetricsResourceResource extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsResourceResource.class);

    @Context
    UriInfo m_uriInfo;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private EventProxy m_eventProxy;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private GraphDao m_graphDao;

    @Context
    ResourceContext m_context;

    /**
     * <p>
     * getMetrics</p>
     *
     * @param nodeCriteria
     *
     * @return a Metrics object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Metrics getMetrics(@PathParam("nodeCriteria") final String nodeCriteria) {
        readLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getMetricsResource: Can't find node " + nodeCriteria);
            }
            return getMetricsCollectionResource(node);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>
     * getMetrics</p>
     *
     * @param nodeCriteria
     * @param reportCriteria
     *
     * @return a Metrics object.
     */
    @GET
    @Path("/{reportCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Metrics getMetrics(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("reportCriteria") String reportCriteria) {
        LOG.debug("getMetrics: reportCriteria: {}", reportCriteria);
        readLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getMetricsResource: Can't find node " + nodeCriteria);
            }
            if (reportCriteria == null || reportCriteria.isEmpty()) {
                return getMetricsCollectionResource(node);
            } else {
                return getMetricsCollectionResourceWithGraphs(node, reportCriteria);
            }
        } finally {
            readUnlock();
        }
    }

    private Metrics getMetricsCollectionResource(final OnmsNode node) {
        OnmsResource resource = m_resourceDao.getResourceForNode(node);

        return new Metrics(node, resource);
    }

    private Metrics getMetricsCollectionResourceWithGraphs(final OnmsNode node, final String reportCriteria) {
        LOG.debug("getGraphsCollectionResource: {}", reportCriteria);

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        Metrics metrics = new Metrics(node, resource, reportCriteria);
        for (MetricResource mr : metrics.getResourceList()) {
            for (Metric metric : mr.getMetricList()) {
                PrefabGraph[] queries = m_graphDao.getPrefabGraphsForResource(metric.getResource());
                for (PrefabGraph query : queries) {
                    metric.addGraph(query.getName());
                }

            }
        }

        return metrics;
    }

    @XmlRootElement(name = "metrics")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Metrics {

        @XmlAttribute(name = "nodeId", required = true)
        private Integer m_nodeId;

        @XmlAttribute(name = "resourceId", required = true)
        private String m_resourceId;

        @XmlAttribute(name = "time", required = true)
        private Long m_timestamp;

        @XmlElements(
                @XmlElement(name = "resource"))
        private List<MetricResource> m_resources = new ArrayList<MetricResource>();

        public Metrics() {
        }

        public Metrics(final OnmsNode node, final OnmsResource resource) {
            m_nodeId = node.getId();
            m_resourceId = resource.getId();
            m_timestamp = new Date().getTime();

            /* we should order m_resources, nodeSnmp should always be first? */
            for (OnmsResource r : resource.getChildResources()) {
                OnmsResourceType type = r.getResourceType();
                boolean found = false;
                for(MetricResource mr : m_resources) {
                    if (mr.getName().equals(type.getName())) {
                        found = true;
                        mr.addMetric(new Metric(r));
                        break;
                    }
                }
                if (!found) {
                    MetricResource mr = new MetricResource(type);
                    m_resources.add(mr);
                    mr.addMetric(new Metric(r));
                }
            }
        }

        public Metrics(final OnmsNode node, final OnmsResource resource, final String reportCriteria) {
            m_nodeId = node.getId();
            m_resourceId = resource.getId();
            m_timestamp = new Date().getTime();
            String resourceName = resource.getId()+'.'+reportCriteria;

            /* we should order m_resources, nodeSnmp should always be first? */
            for (OnmsResource childResource : resource.getChildResources()) {
                LOG.debug("comparing child resource: '{}' to '{}'", childResource.getId(), resourceName);
                if ("*".equals(reportCriteria) || childResource.getId().equals(resourceName)) {
                    LOG.debug("found child resource: '{}' to '{}'", childResource.getId(), resourceName);
                    OnmsResourceType type = childResource.getResourceType();
                    boolean found = false;
                    for (MetricResource mr : m_resources) {
                        if (mr.getName().equals(type.getName())) {
                            found = true;
                            mr.addMetric(new Metric(childResource));
                            break;
                        }
                    }
                    if (!found) {
                        MetricResource mr = new MetricResource(type);
                        m_resources.add(mr);
                        mr.addMetric(new Metric(childResource));

                    }
                }
            }
        }

        public List<MetricResource> getResourceList() {
            return m_resources;
        }
    }

    @XmlRootElement(name = "resource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class MetricResource {

        @XmlAttribute(name = "name", required = true)
        private String m_name;

        @XmlAttribute(name = "label", required = true)
        private String m_label;

        @XmlElements(
                @XmlElement(name = "metric"))
        private List<Metric> m_metrics = new ArrayList<Metric>();

        public MetricResource() {

        }

        public MetricResource(OnmsResourceType type) {
            m_name = type.getName();
            m_label = type.getLabel();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
            hash = 29 * hash + (this.m_label != null ? this.m_label.hashCode() : 0);
            return hash;
        }

        public String getName() {
            return m_name;
        }

        private void addMetric(Metric metric) {
            m_metrics.add(metric);
        }

        public List<Metric> getMetricList() {
            return m_metrics;
        }
    }

    @XmlRootElement(name = "metric")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class Metric {

        @XmlAttribute(name = "resourceId", required = true)
        private String m_resourceId;

        @XmlAttribute(name = "resourceLabel", required = true)
        private String m_resourceLabel;

        @XmlAttribute(name = "resourceName", required = true)
        private String m_resourceName;

        @XmlElements(@XmlElement(name = "graph"))
        private List<String> m_graph = new ArrayList<String>();

        private OnmsResource m_resource;

        Metric() {
        }

        Metric(final OnmsResource resource) {
            m_resourceId = resource.getId();
            m_resourceLabel = resource.getLabel();
            m_resourceName = resource.getName();
            m_resource = resource;

        }

        public OnmsResource getResource() {
            return m_resource;
        }

        private void addGraph(final String name) {
            m_graph.add(name);
        }
    }
}
