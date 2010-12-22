
//
//This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// Modifications:
//
// 2009 Oct 09: check for isManaged = "D" - ayres@opennms.org
// 
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc.:
// 51 Franklin Street
// 5th Floor
// Boston, MA 02110-1301
// USA
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//


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

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
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

@Component
/**
 * <p>OnmsIpInterfaceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsIpInterfaceResource extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private EventProxy m_eventProxy;

    @Context
    ResourceContext m_context;
    
    @Context 
    UriInfo m_uriInfo;

    /**
     * <p>getIpInterfaces</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterfaceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsIpInterfaceList getIpInterfaces(@PathParam("nodeCriteria") String nodeCriteria) {
        log().debug("getIpInterfaces: reading interfaces for node " + nodeCriteria);
        
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.add(Restrictions.ne("isManaged", "D"));
        setLimitOffset(params, criteria, 20, true);
        addOrdering(params, criteria, true);
        
        addFiltersToCriteria(params, criteria, OnmsIpInterface.class);
        criteria.createCriteria("node").add(Restrictions.eq("id", node.getId()));
        OnmsIpInterfaceList interfaceList = new OnmsIpInterfaceList(m_ipInterfaceDao.findMatching(criteria));

        //kind of a hack to get the total count of items. rework this
        OnmsCriteria crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.ne("isManaged", "D"));
        crit.createCriteria("node").add(Restrictions.eq("id", node.getId()));

        addFiltersToCriteria(params, crit, OnmsIpInterface.class);
        int count = m_ipInterfaceDao.countMatching(crit);
        interfaceList.setTotalCount(count);
        
        return interfaceList;
    }

    /**
     * <p>getIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ipAddress}")
    public OnmsIpInterface getIpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "getIpInterface: can't find node " + nodeCriteria);
        }
        return node.getIpInterfaceByIpAddress(ipAddress);
    }

    /**
     * <p>addIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addIpInterface(@PathParam("nodeCriteria") String nodeCriteria, OnmsIpInterface ipInterface) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "addIpInterface: can't find node " + nodeCriteria);
        } else if (ipInterface == null) {
            throwException(Status.BAD_REQUEST, "addIpInterface: ipInterface object cannot be null");
        } else if (ipInterface.getIpAddress() == null) {
            throwException(Status.BAD_REQUEST, "addIpInterface: ipInterface's ipAddress cannot be null");
        } else if (ipInterface.getIpAddress().getAddress() == null) {
            throwException(Status.BAD_REQUEST, "addIpInterface: ipInterface's ipAddress bytes cannot be null");
        }
        log().debug("addIpInterface: adding interface " + ipInterface);
        node.addIpInterface(ipInterface);
        m_ipInterfaceDao.save(ipInterface);
        Event e = new Event();
        e.setUei(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
        e.setNodeid(node.getId());
        e.setInterface(ipInterface.getIpAddressAsString());
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }
    
    /**
     * <p>updateIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{ipAddress}")
    public Response updateIpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteIpInterface: can't find node " + nodeCriteria);
        }
        OnmsIpInterface ipInterface = node.getIpInterfaceByIpAddress(ipAddress);
        if (ipInterface == null) {
            throwException(Status.CONFLICT, "deleteIpInterface: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
        }
        log().debug("updateIpInterface: updating ip interface " + ipInterface);
        BeanWrapper wrapper = new BeanWrapperImpl(ipInterface);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateIpInterface: ip interface " + ipInterface + " updated");
        m_ipInterfaceDao.saveOrUpdate(ipInterface);
        return Response.ok().build();
    }

    /**
     * <p>deleteIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{ipAddress}")
    public Response deleteIpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteIpInterface: can't find node " + nodeCriteria);
        }
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
        if (intf == null) {
            throwException(Status.CONFLICT, "deleteIpInterface: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
        }
        log().debug("deleteIpInterface: deleting interface " + ipAddress + " from node " + nodeCriteria);
        node.getIpInterfaces().remove(intf);
        m_nodeDao.save(node);
        Event e = new Event();
        e.setUei(EventConstants.INTERFACE_DELETED_EVENT_UEI);
        e.setNodeid(node.getId());
        e.setInterface(ipAddress);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }
    
    /**
     * <p>getServices</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsMonitoredServiceResource} object.
     */
    @Path("{ipAddress}/services")
    public OnmsMonitoredServiceResource getServices() {
        return m_context.getResource(OnmsMonitoredServiceResource.class);
    }

}
