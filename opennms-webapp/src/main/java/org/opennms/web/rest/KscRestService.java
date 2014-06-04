/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.ws.rs.GET;
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
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.svclayer.KscReportService;
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
            final KscReportCollection reports = new KscReportCollection(m_kscReportService.getReportList());
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
            final Map<Integer, String> reportList = m_kscReportService.getReportList();
            final String label = reportList.get(reportId);
            if (label == null) {
                throw getException(Status.NOT_FOUND, "No such report id " + reportId);
            }
            return new KscReport(reportId, label);
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
                LogUtils.debugf(this, "invalid timespan ('%s'), setting to '7_day' instead.", timespan);
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

    @Entity
    @XmlRootElement(name = "kscReports")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class KscReportCollection extends LinkedList<KscReport> {
        private static final long serialVersionUID = -4169259948312457702L;

        private int m_totalCount;

        public KscReportCollection() {
            super();
        }

        public KscReportCollection(final Collection<? extends KscReport> c) {
            super(c);
        }

        public KscReportCollection(final Map<Integer, String> reportList) {
            for (final Integer key : reportList.keySet()) {
                add(new KscReport(key, reportList.get(key)));
            }
        }

        @XmlElement(name = "kscReport")
        public List<KscReport> getKscReports() {
            return this;
        }

        public void setKscReports(final List<KscReport> reports) {
            if (reports == this) return;
            clear();
            addAll(reports);
        }

        @XmlAttribute(name = "count")
        public Integer getCount() {
            return this.size();
        }

        @XmlAttribute(name = "totalCount")
        public int getTotalCount() {
            return m_totalCount;
        }

        public void setTotalCount(final int count) {
            m_totalCount = count;
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

        public KscReport() {
        }

        public KscReport(final Integer reportId, final String label) {
            m_id = reportId;
            m_label = label;
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
    }
}
