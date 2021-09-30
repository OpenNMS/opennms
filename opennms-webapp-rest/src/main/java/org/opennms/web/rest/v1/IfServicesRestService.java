/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
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

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetail;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetailList;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Managing Monitored Services (control the polling state of monitored services).
 * 
 * Examples:
 *
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?node.label=onms-prd-01"
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?ipInterface.ipAddress=192.168.32.140"
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * 
 * curl -X PUT "status=F" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?node.label=onms-prd-01"
 * curl -X PUT "status=A" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?ipInterface.ipAddress=192.168.32.140"
 * curl -X PUT "status=F" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * curl -X PUT "status=F&services=ICMP,HTTP" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * 
 * Possible values for status:
 * A (Managed)
 * F (Forced Unmanaged)
 * R (Rescan to Resume, for compatibility purposes)
 * S (Rescan to Suspend, for compatibility purposes)
 * 
 * The optional parameter services is designed to specify the list of affected services as CSV.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("ifServicesRestService")
@Path("ifservices")
@Transactional
public class IfServicesRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(IfServicesRestService.class);

    @Autowired
    private MonitoredServiceDao m_serviceDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public OnmsMonitoredServiceDetailList getServices(@Context final UriInfo uriInfo) {
        final Criteria c = getCriteria(uriInfo.getQueryParameters());
        final OnmsMonitoredServiceDetailList servicesList = new OnmsMonitoredServiceDetailList();
        final List<OnmsMonitoredService> services = m_serviceDao.findMatching(c);
        for (OnmsMonitoredService svc : services) {
            servicesList.add(new OnmsMonitoredServiceDetail(svc));
        }
        c.setLimit(null);
        c.setOffset(null);
        c.setOrders(new ArrayList<Order>());
        servicesList.setTotalCount(m_serviceDao.countMatching(c));
        return servicesList;
    }

    @GET
    @Path("/{id}")
    public Response getServiceById(@PathParam("id") Integer monitoredServiceId) {
        OnmsMonitoredService monitoredService = m_serviceDao.get(monitoredServiceId);
        if (monitoredService == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().entity(new OnmsMonitoredServiceDetail(monitoredService)).build();
    }

    @PUT
    public Response updateServices(@Context final UriInfo uriInfo, final MultivaluedMapImpl params) {
        final String status = params.getFirst("status");
        if (status == null || !status.matches("(A|R|S|F)")) {
            throw getException(Status.BAD_REQUEST, "Parameter status must be specified. Possible values: A (Managed), F (Forced Unmanaged), R (Rescan to Resume), S (Rescan to Suspend)");
        }
        final String services_csv = params.getFirst("services");
        final List<String> serviceList = new ArrayList<>();
        if (services_csv != null) {
            for (String s : services_csv.split(",")) {
                serviceList.add(s);
            }
        }
        final Criteria c = getCriteria(uriInfo.getQueryParameters());
        c.setLimit(null);
        c.setOffset(null);
        final OnmsMonitoredServiceList services = new OnmsMonitoredServiceList(m_serviceDao.findMatching(c));
        if (services.isEmpty()) {
            throw getException(Status.BAD_REQUEST, "Can't find any service matching the provided criteria: {}.", uriInfo.getQueryParameters().toString());
        }
        boolean modified = false;
        for (OnmsMonitoredService svc : services) {
            boolean proceed = false;
            if (serviceList.isEmpty()) {
                proceed = true;
            } else {
                if (serviceList.contains(svc.getServiceName())) {
                    proceed = true;
                }
            }
            if (proceed) {
                modified = true;
                final String currentStatus = svc.getStatus();
                svc.setStatus(status);
                m_serviceDao.update(svc);
                if ("S".equals(status) || ("A".equals(currentStatus) && "F".equals(status))) {
                    LOG.debug("updateServices: suspending polling for service {} on node with IP {}", svc.getServiceName(), svc.getIpAddress().getHostAddress());
                    sendEvent(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, svc); // TODO ManageNodeServlet is sending this.
                    sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, svc);
                }
                if ("R".equals(status) || ("F".equals(currentStatus) && "A".equals(status))) {
                    LOG.debug("updateServices: resuming polling for service {} on node with IP {}", svc.getServiceName(), svc.getIpAddress().getHostAddress());
                    sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, svc);
                }
            }
        }
        return modified ? Response.noContent().build() : Response.notModified().build();
    }

    private static Criteria getCriteria(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class);
        builder.alias("ipInterface.snmpInterface", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node.categories", "category", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.orderBy("id");
        applyQueryFilters(params, builder);

        return builder.toCriteria();
    }

    private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
        final EventBuilder bldr = new EventBuilder(eventUEI, "ReST");
        bldr.setNodeid(dbObj.getNodeId());
        bldr.setInterface(dbObj.getIpAddress());
        bldr.setService(dbObj.getServiceName());
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (EventProxyException ex) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", eventUEI, ex.getMessage());
        }
    }

}
