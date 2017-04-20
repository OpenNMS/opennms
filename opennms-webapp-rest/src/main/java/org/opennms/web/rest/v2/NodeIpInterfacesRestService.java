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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeIpInterfacesRestService extends AbstractNodeDependentRestService<OnmsIpInterface,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeIpInterfacesRestService.class);

    @Autowired
    private IpInterfaceDao m_dao;

    @Override
    protected IpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsIpInterface> getDaoClass() {
        return OnmsIpInterface.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        builder.alias("monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);
        updateCriteria(uriInfo, builder);
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsIpInterface> createListWrapper(Collection<OnmsIpInterface> list) {
        return new OnmsIpInterfaceList(list);
    }

    protected Response doCreate(UriInfo uriInfo, OnmsIpInterface ipInterface) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (ipInterface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface object cannot be null");
        } else if (ipInterface.getIpAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress cannot be null");
        } else if (ipInterface.getIpAddress().getAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress bytes cannot be null");
        }
        node.addIpInterface(ipInterface);
        getDao().save(ipInterface);

        final Event event = EventUtils.createNodeGainedInterfaceEvent("ReST", node.getId(), ipInterface.getIpAddress());
        sendEvent(event);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, ipInterface.getIpAddress().getHostAddress())).build();
    }

    // Overrides default implementation
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response getByIpAddress(@Context final UriInfo uriInfo, @PathParam("id") final String ipAddress) {
        final OnmsIpInterface iface = getInterface(uriInfo, ipAddress);
        if (iface == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(iface).build();
    }

    // Overrides default implementation
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    public Response update(@Context final UriInfo uriInfo, @PathParam("id") final String ipAddress, final OnmsIpInterface object) {
        writeLock();
        try {
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            OnmsIpInterface retval = getInterface(uriInfo, ipAddress);
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
    public Response updateProperties(@Context final UriInfo uriInfo, @PathParam("id") final String ipAddress, final MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsIpInterface object = getInterface(uriInfo, ipAddress);
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
    public Response delete(@Context final UriInfo uriInfo, @PathParam("id") final String ipAddress) {
        writeLock();
        try {
            OnmsIpInterface object = getInterface(uriInfo, ipAddress);
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            LOG.debug("delete: deleting object {}", object);
            object.getNode().getIpInterfaces().remove(object);
            getDao().delete(object);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @Path("{id}/services")
    public NodeMonitoredServiceRestService getMonitoredServicesResource(@Context final ResourceContext context) {
        return context.getResource(NodeMonitoredServiceRestService.class);
    }

    private OnmsIpInterface getInterface(final UriInfo uriInfo, @PathParam("id") final String ipAddress) {
        final OnmsNode node = getNode(uriInfo);
        return node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
    }

}
