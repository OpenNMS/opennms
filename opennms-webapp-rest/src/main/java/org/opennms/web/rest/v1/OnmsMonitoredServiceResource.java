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

package org.opennms.web.rest.v1;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>OnmsMonitoredServiceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Component("onmsMonitoredServiceResource")
@Transactional
public class OnmsMonitoredServiceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsMonitoredServiceResource.class);

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao m_serviceDao;
    
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * <p>getServices</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredServiceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsMonitoredServiceList getServices(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
        }
        return new OnmsMonitoredServiceList(iface.getMonitoredServices());
    }

    /**
     * <p>getService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{service}")
    public OnmsMonitoredService getService(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, @PathParam("service") String service) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
        }
        final OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(service);
        if (svc == null) {
            throw getException(Status.NOT_FOUND, "Monitored Service {} was not found on IP Interface {} and node {}.", service, ipAddress, nodeCriteria);
        }
        return svc;
    }
    
    /**
     * <p>addService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addService(@Context final UriInfo uriInfo, @PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ipAddress") final String ipAddress, final OnmsMonitoredService service) {
        writeLock();
        
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            final OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            if (service == null) throw getException(Status.BAD_REQUEST, "Service object cannot be null");
            if (service.getServiceName() == null) throw getException(Status.BAD_REQUEST, "Service must have a name");

            final OnmsServiceType serviceType = new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {
                @Override
                protected OnmsServiceType query() {
                    return m_dao.findByName(service.getServiceName());
                }

                @Override
                protected OnmsServiceType doInsert() {
                    LOG.info("addService: creating service type {}", service.getServiceName());
                    final OnmsServiceType s = new OnmsServiceType(service.getServiceName());
                    m_dao.saveOrUpdate(s);
                    return s;
                }
            }.execute();

            service.setServiceType(serviceType);
            service.setIpInterface(intf);
            LOG.debug("addService: adding service {}", service);
            m_serviceDao.save(service);
            
            Event e = EventUtils.createNodeGainedServiceEvent("ReST", node.getId(), intf.getIpAddress(), 
                    service.getServiceName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
            sendEvent(e);

            return Response.created(getRedirectUri(uriInfo, service.getServiceName())).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{service}")
    public Response updateService(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, @PathParam("service") String serviceName, MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
            if (service == null) throw getException(Status.BAD_REQUEST, "Monitored Service {} was not found on IP Interface {} and node {}.", serviceName, ipAddress, nodeCriteria);
    
            LOG.debug("updateService: updating service {}", service);
            boolean modified = false;
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(service);
            for(String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    if (key.equals("status")) {
                        if ("S".equals(value) || ("A".equals(service.getStatus()) && "F".equals(value))) {
                            LOG.debug("updateService: suspending polling for service {} on node with IP {}", service.getServiceName(), service.getIpAddress().getHostAddress());
                            value = "F";
                            sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, service);
                        }
                        if ("R".equals(value) || ("F".equals(service.getStatus()) && "A".equals(value))) {
                            LOG.debug("updateService: resuming polling for service {} on node with IP {}", service.getServiceName(), service.getIpAddress().getHostAddress());
                            value = "A";
                            sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, service);
                        }
                    }
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                LOG.debug("updateSservice: service {} updated", service);
                m_serviceDao.saveOrUpdate(service);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{service}")
    public Response deleteService(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("ipAddress") final String ipAddress, @PathParam("service") final String serviceName) {
        writeLock();
        
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
            if (service == null) throw getException(Status.CONFLICT, "Monitored Service {} was not found on IP Interface {} and node {}.", serviceName, ipAddress, nodeCriteria);
            LOG.debug("deleteService: deleting service {} from node {}", serviceName, nodeCriteria);

            Event e = EventUtils.createDeleteServiceEvent("OpenNMS.REST", node.getId(), ipAddress, serviceName, -1L);
            sendEvent(e);

            return Response.accepted().build();
        } finally {
            writeUnlock();
        }
    }

    private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
        final EventBuilder bldr = new EventBuilder(eventUEI, "ReST");
        bldr.setNodeid(dbObj.getNodeId());
        bldr.setInterface(dbObj.getIpAddress());
        bldr.setService(dbObj.getServiceName());
        sendEvent(bldr.getEvent());
    }

    private void sendEvent(Event event) {
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", event.getUei(), e.getMessage());
        }
    }

}
