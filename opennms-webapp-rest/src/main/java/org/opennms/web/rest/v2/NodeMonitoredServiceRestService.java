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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeMonitoredServiceRestService extends AbstractNodeDependentRestService<OnmsMonitoredService,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeMonitoredServiceRestService.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private MonitoredServiceDao m_dao;

    @Override
    protected MonitoredServiceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoredService> getDaoClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.alias("ipInterface", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
        updateCriteria(uriInfo, builder);
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoredService> createListWrapper(Collection<OnmsMonitoredService> list) {
        return new OnmsMonitoredServiceList(list);
    }

    protected Response doCreate(UriInfo uriInfo, OnmsMonitoredService service) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        if (iface == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        service.setServiceType(getServiceType(service.getServiceName()));
        service.setIpInterface(iface);
        iface.addMonitoredService(service);
        getDao().save(service);

        final Event e = EventUtils.createNodeGainedServiceEvent("ReST", iface.getNode().getId(), iface.getIpAddress(), service.getServiceName(), iface.getNode().getLabel(),
                                                                iface.getNode().getLabelSource(), iface.getNode().getSysName(), iface.getNode().getSysDescription());
        sendEvent(e);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, service.getServiceName())).build();
    }

    protected void updateCriteria(final UriInfo uriInfo, final CriteriaBuilder builder) {
        super.updateCriteria(uriInfo, builder);
        List<PathSegment> segments = uriInfo.getPathSegments(true);
        final String ipAddress =  segments.get(3).getPath(); // /nodes/{criteria}/ipinterfaces/{ipAddress}
        builder.eq("ipInterface.ipAddress", ipAddress);
    }

    // Overrides default implementation
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response getByName(@Context final UriInfo uriInfo, @PathParam("id") final String serviceName) {
        final OnmsMonitoredService service = getService(uriInfo, serviceName);
        if (service == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(service).build();
    }

    // Overrides default implementation
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    public Response update(@Context final UriInfo uriInfo, @PathParam("id") final String lookupCriteria, final OnmsMonitoredService object) {
        writeLock();
        try {
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            OnmsMonitoredService retval = getService(uriInfo, lookupCriteria);
            if (retval == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            if (!retval.getId().equals(object.getId())) {
                return Response.status(Status.NOT_FOUND).build();
            }
            LOG.debug("update: updating object {}", object);
            getDao().saveOrUpdate(object);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    // Overrides default implementation
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    public Response updateProperties(@Context final UriInfo uriInfo, @PathParam("id") final String lookupCriteria, final MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsMonitoredService object = getService(uriInfo, lookupCriteria);
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            LOG.debug("update: updating object {}", object);
            RestUtils.setBeanProperties(object, params);
            LOG.debug("update: object {} updated", object);
            getDao().saveOrUpdate(object);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    // Overrides default implementation
    @DELETE
    @Path("{id}")
    public Response delete(@Context final UriInfo uriInfo, @PathParam("id") final String lookupCriteria) {
        writeLock();
        try {
            OnmsMonitoredService object = getService(uriInfo, lookupCriteria);
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            LOG.debug("delete: deleting object {}", lookupCriteria);
            object.getIpInterface().getMonitoredServices().remove(object);
            getDao().delete(object);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        final OnmsServiceType serviceType = new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {
            @Override
            protected OnmsServiceType query() {
                return m_dao.findByName(serviceName);
            }
            @Override
            protected OnmsServiceType doInsert() {
                LOG.info("getServiceType: creating service type {}", serviceName);
                final OnmsServiceType s = new OnmsServiceType(serviceName);
                m_dao.saveOrUpdate(s);
                return s;
            }
        }.execute();
        return serviceType;
    }

    private OnmsMonitoredService getService(final UriInfo uriInfo, final String serviceName) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        return iface == null ? null : iface.getMonitoredServiceByServiceType(serviceName);
    }

    private OnmsIpInterface getInterface(final UriInfo uriInfo) {
        final OnmsNode node = getNode(uriInfo);
        final String ipAddress =  uriInfo.getPathSegments(true).get(3).getPath();
        return node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
    }

}
