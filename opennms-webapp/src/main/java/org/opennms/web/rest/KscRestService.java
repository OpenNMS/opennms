/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    
	@Context
	UriInfo m_uriInfo;

	@Context
	HttpHeaders m_headers;

	@Context
	SecurityContext m_securityContext;

    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional
    public KscReportCollection getReports() throws ParseException {
        final KscReportCollection reports = new KscReportCollection(m_kscReportService.getReportList());
        reports.setTotalCount(reports.size());
        return reports;
    }

	@GET
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("{index}")
	@Transactional
	public KscReport getReport(@PathParam("index") final Integer reportIndex) {
        final Map<Integer, String> reportList = m_kscReportService.getReportList();
        return new KscReport(reportIndex, reportList.get(reportIndex));
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("count")
	@Transactional
	public String getCount() {
	    return Integer.toString(m_kscReportService.getReportList().size());
	}

	@Entity
	@XmlRootElement(name="kscReports")
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

        @XmlElement(name="kscReport")
	    public List<KscReport> getKscReports() {
	        return this;
	    }

	    public void setKscReports(final List<KscReport> reports) {
	        clear();
	        addAll(reports);
	    }
	    
	    @XmlAttribute(name="count")
	    public Integer getCount() {
	        return this.size();
	    }
	    
	    @XmlAttribute(name="totalCount")
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
        @XmlAttribute(name="id", required=true)
        private Integer m_id;
        @XmlAttribute(name="label", required=true)
        private String m_label;

        public KscReport() {}

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
