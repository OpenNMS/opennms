
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

import static org.opennms.core.utils.InetAddressUtils.addr;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsIpInterfaceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsIpInterfaceResource.class);


    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    @Qualifier("eventProxy")
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
    public OnmsIpInterfaceList getIpInterfaces(@PathParam("nodeCriteria") final String nodeCriteria) {
        readLock();
        
        try {
            LOG.debug("getIpInterfaces: reading interfaces for node {}", nodeCriteria);
    
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            
            final MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
            
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
            builder.alias("monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);
            builder.ne("isManaged", "D");
            builder.limit(20);
            applyQueryFilters(params, builder);
            builder.alias("node", "node");
            builder.eq("node.id", node.getId());
            
            final OnmsIpInterfaceList interfaceList = new OnmsIpInterfaceList(m_ipInterfaceDao.findMatching(builder.toCriteria()));
    
            interfaceList.setTotalCount(m_ipInterfaceDao.countMatching(builder.count().toCriteria()));
            
            return interfaceList;
        } finally {
            readUnlock();
        }
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
    public OnmsIpInterface getIpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ipAddress") final String ipAddress) {
        readLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getIpInterface: can't find node " + nodeCriteria);
            }
            return node.getIpInterfaceByIpAddress(InetAddressUtils.getInetAddress(ipAddress));
        } finally {
            readUnlock();
        }
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
    public Response addIpInterface(@PathParam("nodeCriteria") final String nodeCriteria, final OnmsIpInterface ipInterface) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "addIpInterface: can't find node " + nodeCriteria);
            } else if (ipInterface == null) {
                throw getException(Status.BAD_REQUEST, "addIpInterface: ipInterface object cannot be null");
            } else if (ipInterface.getIpAddress() == null) {
                throw getException(Status.BAD_REQUEST, "addIpInterface: ipInterface's ipAddress cannot be null");
            } else if (ipInterface.getIpAddress().getAddress() == null) {
                throw getException(Status.BAD_REQUEST, "addIpInterface: ipInterface's ipAddress bytes cannot be null");
            }
            LOG.debug("addIpInterface: adding interface {}", ipInterface);
            node.addIpInterface(ipInterface);
            m_ipInterfaceDao.save(ipInterface);
            
            final EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, getClass().getName());
    
            bldr.setNodeid(node.getId());
            bldr.setInterface(ipInterface.getIpAddress());
    
            try {
                m_eventProxy.send(bldr.getEvent());
            } catch (final EventProxyException ex) {
                throw getException(Status.BAD_REQUEST, ex.getMessage());
            }
            return Response.seeOther(getRedirectUri(m_uriInfo, InetAddressUtils.str(ipInterface.getIpAddress()))).build();
        } finally {
            writeUnlock();
        }
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
    public Response updateIpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ipAddress") final String ipAddress, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "deleteIpInterface: can't find node " + nodeCriteria);
            }
            final OnmsIpInterface ipInterface = node.getIpInterfaceByIpAddress(ipAddress);
            if (ipInterface == null) {
                throw getException(Status.CONFLICT, "deleteIpInterface: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
            }
            LOG.debug("updateIpInterface: updating ip interface {}", ipInterface);
    
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(ipInterface);
    
            for(final String key : params.keySet()) {
                // skip nodeId since we already know the node this is associated with and don't want to overwrite it
                if ("nodeId".equals(key)) {
                    continue;
                }
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            LOG.debug("updateIpInterface: ip interface {} updated", ipInterface);
            m_ipInterfaceDao.saveOrUpdate(ipInterface);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
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
    public Response deleteIpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ipAddress") final String ipAddress) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "deleteIpInterface: can't find node " + nodeCriteria);
            }
            final OnmsIpInterface intf = node.getIpInterfaceByIpAddress(InetAddressUtils.getInetAddress(ipAddress));
            if (intf == null) {
                throw getException(Status.CONFLICT, "deleteIpInterface: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
            }
            LOG.debug("deleteIpInterface: deleting interface {} from node {}", ipAddress, nodeCriteria);
            node.getIpInterfaces().remove(intf);
            m_nodeDao.save(node);
            
            final EventBuilder bldr = new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, getClass().getName());
    
            bldr.setNodeid(node.getId());
            bldr.setInterface(addr(ipAddress));
    
            try {
                m_eventProxy.send(bldr.getEvent());
            } catch (final EventProxyException ex) {
                throw getException(Status.BAD_REQUEST, ex.getMessage());
            }
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>getServices</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsMonitoredServiceResource} object.
     */
    @Path("{ipAddress}/services")
    public OnmsMonitoredServiceResource getServices() {
        readLock();
        try {
            return m_context.getResource(OnmsMonitoredServiceResource.class);
        } finally {
            readUnlock();
        }
    }

}
