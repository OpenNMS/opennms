/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.web.rest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.svclayer.api.KscReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("ksc")
public class KscRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(KscRestService.class);

    @Autowired
    private KscReportService m_kscReportService;

    @Autowired
    private KSC_PerformanceReportFactory m_kscReportFactory;

    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public KscReportCollection getReports() throws ParseException {
        readLock();

        try {
            final KscReportCollection reports = new KscReportCollection(m_kscReportService.getReportMap(), true);
            reports.setTotalCount(reports.size());
            return reports;
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{reportId}")
    @Transactional
    public KscReport getReport(@PathParam("reportId") final Integer reportId) {
        readLock();

        try {
            final Map<Integer, Report> reportList = m_kscReportService.getReportMap();
            final Report report = reportList.get(reportId);
            if (report == null) {
                throw getException(Status.NOT_FOUND, "No such report id " + reportId);
            }
            return new KscReport(report);
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        readLock();
        try {
            return Integer.toString(m_kscReportService.getReportList().size());
        } finally {
            readUnlock();
        }
    }

    @PUT
    @Path("{kscReportId}")
    @Transactional
    public Response addGraph(@PathParam("kscReportId") final Integer kscReportId, @QueryParam("title") final String title, @QueryParam("reportName") final String reportName, @QueryParam("resourceId") final String resourceId, @QueryParam("timespan") String timespan) {
        writeLock();

        try {
            if (kscReportId == null || reportName == null || reportName == "" || resourceId == null || resourceId == "") {
                throw getException(Status.BAD_REQUEST, "Invalid request: reportName and resourceId cannot be empty!");
            }
            final Report report = m_kscReportFactory.getReportByIndex(kscReportId);
            if (report == null) {
                throw getException(Status.NOT_FOUND, "Invalid request: No KSC report found with ID: " + kscReportId);
            }
            final Graph graph = new Graph();
            if (title != null) {
                graph.setTitle(title);
            }

            boolean found = false;
            for (final String valid : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
                if (valid.equals(timespan)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.debug("invalid timespan ('{}'), setting to '7_day' instead.", timespan);
                timespan = "7_day";
            }

            graph.setGraphtype(reportName);
            graph.setResourceId(resourceId);
            graph.setTimespan(timespan);
            report.addGraph(graph);
            m_kscReportFactory.setReport(kscReportId, report);
            try {
                m_kscReportFactory.saveCurrent();
            } catch (final Exception e) {
                throw getException(Status.BAD_REQUEST, e.getMessage());
            }
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addKscReport(final KscReport kscReport) {
        writeLock();
        try {
            LOG.debug("addKscReport: Adding KSC Report {}", kscReport);
            Report report = m_kscReportFactory.getReportByIndex(kscReport.getId());
            if (report != null) {
                throw getException(Status.CONFLICT, "Invalid request: Existing KSC report found with ID: " + kscReport.getId());
            }
            report = new Report();
            report.setId(kscReport.getId());
            report.setTitle(kscReport.getLabel());
            if (kscReport.getShowGraphtypeButton() != null) {
                report.setShow_graphtype_button(kscReport.getShowGraphtypeButton());
            }
            if (kscReport.getShowTimespanButton() != null) {
                report.setShow_timespan_button(kscReport.getShowTimespanButton());
            }
            if (kscReport.getGraphsPerLine() != null) {
                report.setGraphs_per_line(kscReport.getGraphsPerLine());
            }
            if (kscReport.hasGraphs()) {
                for (KscGraph kscGraph : kscReport.getGraphs()) {
                    final Graph graph = kscGraph.buildGraph();
                    report.addGraph(graph);
                }
            }

            m_kscReportFactory.addReport(report);
            try {
                m_kscReportFactory.saveCurrent();
            } catch (final Exception e) {
                throw getException(Status.BAD_REQUEST, e.getMessage());
            }
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (final Throwable t) {
            throw getException(Status.BAD_REQUEST, t);
        } finally {
            writeUnlock();
        }
    }

    @Entity
    @XmlRootElement(name = "kscReports")
    public static final class KscReportCollection extends JaxbListWrapper<KscReport> {

        private static final long serialVersionUID = 1L;

        public KscReportCollection() {
            super();
        }

        public KscReportCollection(Collection<? extends KscReport> reports) {
            super(reports);
        }

        public KscReportCollection(final Map<Integer, Report> reportList, boolean terse) {
            super();
            for (final Report report : reportList.values()) {
                if (terse) {
                    add(new KscReport(report.getId(), report.getTitle()));
                } else {
                    add(new KscReport(report));
                }
            }
        }

        @XmlElement(name = "kscReport")
        public List<KscReport> getObjects() {
            return super.getObjects();
        }
    }

    @Entity
    @XmlRootElement(name = "kscReport")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class KscReport {

        @XmlAttribute(name = "id", required = true)
        private Integer m_id;

        @XmlAttribute(name = "label", required = true)
        private String m_label;

        @XmlAttribute(name = "show_timespan_button", required = false)
        private Boolean m_show_timespan_button;

        @XmlAttribute(name = "show_graphtype_button", required = false)
        private Boolean m_show_graphtype_button;

        @XmlAttribute(name = "graphs_per_line", required = false)
        private Integer m_graphs_per_line;

        @XmlElements(@XmlElement(name = "kscGraph"))
        private List<KscGraph> m_graphs = new ArrayList<KscGraph>();

        public KscReport() {
        }

        public KscReport(final Integer reportId, final String label) {
            m_id = reportId;
            m_label = label;
        }

        public KscReport(Report report) {
            m_id = report.getId();
            m_label = report.getTitle();
            m_show_timespan_button = report.getShow_timespan_button();
            m_show_graphtype_button = report.getShow_graphtype_button();
            m_graphs_per_line = report.getGraphs_per_line();
            m_graphs.clear();

            for(Graph graph : report.getGraphCollection()) {
                m_graphs.add(new KscGraph(graph));
            }
        }

        public Integer getId() {
            return m_id;
        }

        public void setId(final Integer id) {
            m_id = id;
        }

        public String getLabel() {
            return m_label;
        }

        public void setLabel(final String label) {
            m_label = label;
        }

        public Boolean getShowTimespanButton() {
            return m_show_timespan_button;
        }

        public void setShowTimespanButton(final Boolean show) {
            m_show_timespan_button = show;
        }

        public Boolean getShowGraphtypeButton() {
            return m_show_graphtype_button;
        }

        public void setShowGraphtypeButton(final Boolean show) {
            m_show_graphtype_button = show;
        }

        public Integer getGraphsPerLine() {
            return m_graphs_per_line;
        }

        public void setGraphsPerLine(final Integer graphs) {
            m_graphs_per_line = graphs;
        }

        public boolean hasGraphs() {
            return !m_graphs.isEmpty();
        }

        public List<KscGraph> getGraphs() {
            return m_graphs;
        }
    }

    @Entity
    @XmlRootElement(name = "kscGraph")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class KscGraph {

        @XmlAttribute(name = "title", required = true)
        private String m_title;

        @XmlAttribute(name = "timespan", required = true)
        private String m_timespan;

        @XmlAttribute(name = "graphtype", required = true)
        private String m_graphtype;

        @XmlAttribute(name = "resourceId", required = false)
        private String m_resourceId;

        @XmlAttribute(name = "nodeId", required = false)
        private String m_nodeId;

        @XmlAttribute(name = "nodeSource", required = false)
        private String m_nodeSource;

        @XmlAttribute(name = "domain", required = false)
        private String m_domain;

        @XmlAttribute(name = "interfaceId", required = false)
        private String m_interfaceId;

        @XmlAttribute(name = "extlink", required = false)
        private String m_extlink;

        public KscGraph() {

        }

        public KscGraph(Graph graph) {
            m_title = graph.getTitle();
            m_timespan = graph.getTimespan();
            m_graphtype = graph.getGraphtype();
            m_resourceId = graph.getResourceId();
            m_nodeId = graph.getNodeId();
            m_nodeSource = graph.getNodeSource();
            m_domain = graph.getDomain();
            m_interfaceId = graph.getInterfaceId();
            m_extlink = graph.getExtlink();
        }

        public Graph buildGraph() {
            boolean found = false;
            for (final String valid : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
                if (valid.equals(m_timespan)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.debug("invalid timespan ('{}'), setting to '7_day' instead.", m_timespan);
                m_timespan = "7_day";
            }

            final Graph graph = new Graph();
            graph.setTitle(m_title);
            graph.setTimespan(m_timespan);
            graph.setGraphtype(m_graphtype);
            graph.setResourceId(m_resourceId);
            graph.setNodeId(m_nodeId);
            graph.setNodeSource(m_nodeSource);
            graph.setDomain(m_domain);
            graph.setInterfaceId(m_interfaceId);
            graph.setExtlink(m_extlink);

            return graph;
        }
    }
}
