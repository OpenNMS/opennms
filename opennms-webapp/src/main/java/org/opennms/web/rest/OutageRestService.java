/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 31, 2008
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.RecentOutagesFilter;
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
    
    /**
     * <p>getOutage</p>
     *
     * @param outageId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsOutage} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{outageId}")
    @Transactional
    public OnmsOutage getOutage(@PathParam("outageId") String outageId) {
    	OnmsOutage result= m_outageDao.get(Integer.valueOf(outageId));
    	return result;
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces("text/plain")
    @Path("count")
    @Transactional
    public String getCount() {
    	return Integer.toString(m_outageDao.countAll());
    }

    /**
     * <p>getOutages</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    public OnmsOutageCollection getOutages() {
    	MultivaluedMap<java.lang.String,java.lang.String> params=m_uriInfo.getQueryParameters();
		OnmsCriteria criteria=new OnmsCriteria(OnmsOutage.class);

    	setLimitOffset(params, criteria);
    	addOrdering(params, criteria, false);
    	addFiltersToCriteria(params, criteria, OnmsOutage.class);

    	return new OnmsOutageCollection(m_outageDao.findMatching(getDistinctIdCriteria(OnmsOutage.class, criteria)));
    }

    /**
     * <p>forNodeId</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("forNode/{nodeId}")
    public OnmsOutageCollection forNodeId(@PathParam("nodeId") int nodeId) {
        MultivaluedMap<java.lang.String,java.lang.String> params=m_uriInfo.getQueryParameters();
        OnmsCriteria criteria=new OnmsCriteria(OnmsOutage.class);

        setLimitOffset(params, criteria);
        addOrdering(params, criteria, false);
        addFiltersToCriteria(params, criteria, OnmsOutage.class);

        criteria.createAlias("monitoredService", "monitoredService");
        criteria.createAlias("monitoredService.ipInterface", "ipInterface");
        criteria.createAlias("monitoredService.ipInterface.node", "node");
        criteria.createAlias("monitoredService.serviceType", "serviceType");

        NodeFilter node = new NodeFilter(nodeId);
        criteria.add(node.getCriterion());

        Date d = new Date(System.currentTimeMillis() - (60 * 60 * 24 * 7));
        RecentOutagesFilter recent = new RecentOutagesFilter(d);
        criteria.add(recent.getCriterion());

        return new OnmsOutageCollection(m_outageDao.findMatching(getDistinctIdCriteria(OnmsOutage.class, criteria)));
    }
}

