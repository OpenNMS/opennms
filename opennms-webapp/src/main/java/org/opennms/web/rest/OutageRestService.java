/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.util.Date;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.opennms.netmgt.model.outage.OutageSummaryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * TODO: Add functionality to getting outages by:
 * nodelabel, nodeid, foreignsource, foreignsource+foreignid, ipaddress, etc.
 * add filters for current, resolved, all
 *
 *<p>REST service to the OpenNMS Outage {@link OnmsOutage} data.</p>
 *<p>This service supports getting the list of outages or one specific outage by ID:</p>
 *<p>Example 1: Query List of outages.</p>
 *<pre>
 *curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/outages
 *</pre>
 *
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("outages")
public class OutageRestService extends OnmsRestService {

    @Autowired
    private OutageDao m_outageDao;
    
    @Context 
    UriInfo m_uriInfo;

    @Context
    SecurityContext m_securityContext;
    
    @Context
    ServletContext m_servletContext;
    
    /**
     * <p>getOutage</p>
     *
     * @param outageId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsOutage} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{outageId}")
    @Transactional
    public Response getOutage(@PathParam("outageId") final String outageId) {
        readLock();
        try {
            if ("summaries".equals(outageId)) {
                final MultivaluedMap<String,String> parms = m_uriInfo.getQueryParameters(true);
                int limit = 10;
                if (parms.containsKey("limit")) {
                    limit = Integer.parseInt(parms.getFirst("limit"));
                }
                return Response.ok(new OutageSummaryCollection(m_outageDao.getNodeOutageSummaries(limit))).build();
            } else {
                return Response.ok(m_outageDao.get(Integer.valueOf(outageId))).build();
            }
        } finally {
            readUnlock();
        }
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        readLock();
        try {
            return Integer.toString(m_outageDao.countAll());
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getOutages</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsOutageCollection getOutages() {
        readLock();
        try {
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
            builder.alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN);
            builder.alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN);
            builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
            builder.alias("ipInterface.snmpInterface", "snmpInterface", JoinType.LEFT_JOIN);
            builder.alias("monitoredService.serviceType", "serviceType", JoinType.LEFT_JOIN);

            applyQueryFilters(m_uriInfo.getQueryParameters(), builder);
    
            final OnmsOutageCollection coll = new OnmsOutageCollection(m_outageDao.findMatching(builder.toCriteria()));
    
            //For getting totalCount
            coll.setTotalCount(m_outageDao.countMatching(builder.count().toCriteria()));
    
            return coll;
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>forNodeId</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Path("forNode/{nodeId}")
    public OnmsOutageCollection forNodeId(@PathParam("nodeId") final int nodeId) {
        readLock();
        
        try {
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
            builder.eq("node.id", nodeId);
            final Date d = new Date(System.currentTimeMillis() - (60 * 60 * 24 * 7));
            builder.or(Restrictions.isNull("ifRegainedService"), Restrictions.gt("ifRegainedService", d));
    
            builder.alias("monitoredService", "monitoredService");
            builder.alias("monitoredService.ipInterface", "ipInterface");
            builder.alias("monitoredService.ipInterface.node", "node");
            builder.alias("monitoredService.serviceType", "serviceType");
    
            applyQueryFilters(m_uriInfo.getQueryParameters(), builder);
    
            builder.orderBy("id").desc();
    
            return new OnmsOutageCollection(m_outageDao.findMatching(builder.toCriteria()));
        } finally {
            readUnlock();
        }
    }
}

