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
 * Created: July 29, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.CriteriaSpecification;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for OnmsNode entity
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("nodes")
@Transactional
public class NodeRestService extends OnmsRestService {
    
    private static final int LIMIT=10;
	
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsNodeList getNodes() {
        OnmsCriteria criteria = getQueryFilters();
        
        OnmsNodeList nodeList = new OnmsNodeList(m_nodeDao.findMatching(criteria));
        
        OnmsCriteria countCrit = getQueryFilters();
        int count = m_nodeDao.countMatching(countCrit);
        nodeList.setTotalCount(count);
        return nodeList;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{nodeCriteria}")
    public OnmsNode getNode(@PathParam("nodeCriteria") String nodeCriteria) {
        return m_nodeDao.get(nodeCriteria);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addNode(OnmsNode node) {
        log().debug("addNode: Adding node " + node);
        m_nodeDao.save(node);
        try {
            sendEvent(EventConstants.NODE_ADDED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok(node).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}")
    public Response updateNode(@PathParam("nodeCriteria") String nodeCriteria, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "updateNode: Can't find node " + nodeCriteria);
        }
        log().debug("updateNode: updating node " + node);
        BeanWrapper wrapper = new BeanWrapperImpl(node);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateNode: node " + node + " updated");
        m_nodeDao.saveOrUpdate(node);
        return Response.ok(node).build();
    }
    
    @DELETE
    @Path("{nodeCriteria}")
    public Response deleteNode(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteNode: Can't find node " + nodeCriteria);
        }
        log().debug("deleteNode: deleting node " + nodeCriteria);
        m_nodeDao.delete(node);
        try {
            sendEvent(EventConstants.NODE_DELETED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }

    @Path("{nodeCriteria}/ipinterfaces")
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    @Path("{nodeCriteria}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }

    @Path("{nodeCriteria}/categories")
    public OnmsCategoryResource getCategoryResource() {
        return m_context.getResource(OnmsCategoryResource.class);
    }

    private OnmsCriteria getQueryFilters() {
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);

    	setLimitOffset(params, criteria, LIMIT, false);
        addOrdering(params, criteria, false);
    	addFiltersToCriteria(params, criteria, OnmsNode.class);

    	criteria.createAlias("snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);
        return getDistinctIdCriteria(OnmsNode.class, criteria);
    }
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        Event e = new Event();
        e.setUei(uei);
        e.setNodeid(nodeId);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        m_eventProxy.send(e);
    }
    
}
