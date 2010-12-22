/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>OnmsMonitoredServiceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsMonitoredServiceResource extends OnmsRestService {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao m_serviceDao;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
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
        return new OnmsMonitoredServiceList(node.getIpInterfaceByIpAddress(ipAddress).getMonitoredServices());
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
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        return node.getIpInterfaceByIpAddress(ipAddress).getMonitoredServiceByServiceType(service);
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
    public Response addService(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, OnmsMonitoredService service) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "addService: can't find node " + nodeCriteria);
        }
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
        if (intf == null) {
            throwException(Status.BAD_REQUEST, "addService: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
        }
        if (service == null) {
            throwException(Status.BAD_REQUEST, "addService: service object cannot be null");
        }
        if (service.getServiceName() == null) {
            throwException(Status.BAD_REQUEST, "addService: service must have a name");
        }
        OnmsServiceType serviceType = m_serviceTypeDao.findByName(service.getServiceName());
        if (serviceType == null)  {
            log().info("addService: creating service type " + service.getServiceName());
            serviceType = new OnmsServiceType(service.getServiceName());
            m_serviceTypeDao.save(serviceType);
        }
        service.setServiceType(serviceType);
        service.setIpInterface(intf);
        log().debug("addService: adding service " + service);
        m_serviceDao.save(service);
        
        Event e = EventUtils.createNodeGainedServiceEvent(getClass().getName(), node.getId(), intf.getIpAddress(), 
                service.getServiceName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
        
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }
    
    /**
     * <p>updateService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{service}")
    public Response updateService(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, @PathParam("service") String serviceName, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "addService: can't find node " + nodeCriteria);
        }
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
        if (intf == null) {
            throwException(Status.BAD_REQUEST, "addService: can't find ip interface on " + nodeCriteria + "@" + ipAddress);
        }
        OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
        if (service == null) {
            throwException(Status.BAD_REQUEST, "addService: can't find service " + serviceName + " on " + nodeCriteria + "@" + ipAddress);
        }

        log().debug("updateService: updating service " + service);
        BeanWrapper wrapper = new BeanWrapperImpl(service);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateSservice: service " + service + " updated");
        m_serviceDao.saveOrUpdate(service);
        return Response.ok().build();
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
    public Response deleteService(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ipAddress") String ipAddress, @PathParam("service") String serviceName) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteService: can't find node " + nodeCriteria);
        }
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
        if (intf == null) {
            throwException(Status.BAD_REQUEST, "deleteService: can't find interface with ip address " + ipAddress + " for node " + nodeCriteria);
        }
        OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
        if (service == null) {
            throwException(Status.CONFLICT, "deleteService: service " + serviceName + " not found on interface " + intf);
        }
        log().debug("deleteService: deleting service " + serviceName + " from node " + nodeCriteria);
        intf.getMonitoredServices().remove(service);
        m_ipInterfaceDao.saveOrUpdate(intf);
        Event e = new Event();
        e.setUei(EventConstants.SERVICE_DELETED_EVENT_UEI);
        e.setNodeid(node.getId());
        e.setInterface(ipAddress);
        e.setService(serviceName);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }
    
}
