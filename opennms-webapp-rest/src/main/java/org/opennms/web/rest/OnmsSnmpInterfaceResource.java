
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
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>OnmsSnmpInterfaceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsSnmpInterfaceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsSnmpInterfaceResource.class);


    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    /**
     * <p>getSnmpInterfaces</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterfaceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsSnmpInterfaceList getSnmpInterfaces(@PathParam("nodeCriteria") final String nodeCriteria) {
        readLock();
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            
            final MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
            
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
            builder.ne("collect", "D");
            builder.limit(20);
            applyQueryFilters(params, builder);
            builder.eq("node.id", node.getId());
            
            final OnmsSnmpInterfaceList snmpList = new OnmsSnmpInterfaceList(m_snmpInterfaceDao.findMatching(builder.toCriteria()));
            
            snmpList.setTotalCount(m_snmpInterfaceDao.countMatching(builder.count().toCriteria()));
    
            return snmpList;
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link org.opennms.netmgt.model.OnmsEntity} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ifIndex}")
    public OnmsEntity getSnmpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ifIndex") final int ifIndex) {
        readLock();
        try {
            return m_nodeDao.get(nodeCriteria).getSnmpInterfaceWithIfIndex(ifIndex);
        } finally {
            readUnlock();
        }
    }
    
    /**
     * <p>addSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addSnmpInterface(@PathParam("nodeCriteria") final String nodeCriteria, final OnmsSnmpInterface snmpInterface) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "addSnmpInterface: can't find node " + nodeCriteria);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "addSnmpInterface: SNMP interface object cannot be null");
            
            LOG.debug("addSnmpInterface: adding interface {}", snmpInterface);
            node.addSnmpInterface(snmpInterface);
            if (snmpInterface.getPrimaryIpInterface() != null) {
                final OnmsIpInterface iface = snmpInterface.getPrimaryIpInterface();
                iface.setSnmpInterface(snmpInterface);
                // TODO Add important events here
            }
            m_snmpInterfaceDao.save(snmpInterface);
            final Integer ifIndex = snmpInterface.getIfIndex();
            return Response.seeOther(getRedirectUri(m_uriInfo, ifIndex)).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>deleteSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{ifIndex}")
    public Response deleteSnmpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ifIndex") final int ifIndex) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find node " + nodeCriteria);
            
            final OnmsEntity snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find SNMP interface with ifIndex " + ifIndex + " for node " + nodeCriteria);
    
            LOG.debug("deletSnmpInterface: deleting interface with ifIndex {} from node {}", ifIndex, nodeCriteria);
            node.getSnmpInterfaces().remove(snmpInterface);
            m_nodeDao.saveOrUpdate(node);
            // TODO Add important events here
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{ifIndex}")
    public Response updateSnmpInterface(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ifIndex") final int ifIndex, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find node " + nodeCriteria);
            if (ifIndex < 0) throw getException(Status.BAD_REQUEST, "deleteSnmpInterface: invalid ifIndex specified for SNMP interface on node " + node.getId() + ": " + ifIndex);
    
            final OnmsSnmpInterface snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find SNMP interface with ifIndex " + ifIndex + " for node " + nodeCriteria);
    
            LOG.debug("updateSnmpInterface: updating SNMP interface {}", snmpInterface);
    
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(snmpInterface);
            for(final String key : params.keySet()) {
                // don't try setting the node data
                if ("nodeId".equals(key)) continue;

                // don't try setting ipinterface data
                if ("ipInterface".equals(key) || "ipInterfaces".equals(key)) continue;

                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            
            Event e = null;
            if (params.containsKey("collect")) {
                // we've updated the collection flag so we need to send an event to redo collection
                final EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "OpenNMS.Webapp");
                bldr.setNode(node);
                // Bug NMS-4432 says that sometimes the primary SNMP interface is null
                // so we need to check for that before we set the interface
                final OnmsIpInterface iface = node.getPrimaryInterface();
                if (iface == null) {
                    LOG.warn("updateSnmpInterface: Cannot send {} event because node {} has no primary SNMP interface", EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, node.getId());
                } else {
                    bldr.setInterface(iface.getIpAddress());
                    e = bldr.getEvent();
                }
            }
            LOG.debug("updateSnmpInterface: SNMP interface {} updated", snmpInterface);
            m_snmpInterfaceDao.saveOrUpdate(snmpInterface);
            
            if (e != null) {
                try {
                    m_eventProxy.send(e);
                } catch (final EventProxyException ex) {
                    throw getException(Response.Status.INTERNAL_SERVER_ERROR, "Exception occurred sending event: "+ex.getMessage());
                }
            }
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

}
