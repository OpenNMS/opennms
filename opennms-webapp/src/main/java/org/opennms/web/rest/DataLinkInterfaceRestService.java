/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>DataLinkInterfaceRestService class.</p>
 *
 * @author antonio
 * @since 1.11.1
 */
@PerRequest
@Scope("prototype")
@Path("links")
@Transactional
public class DataLinkInterfaceRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DataLinkInterfaceRestService.class);

    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Context
    UriInfo m_uriInfo;

    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public DataLinkInterfaceList getLinks() {
        readLock();
        
        try {
            final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
            applyQueryFilters(m_uriInfo.getQueryParameters(), builder);
            builder.orderBy("lastPollTime").desc();
            return new DataLinkInterfaceList(m_dataLinkInterfaceDao.findMatching(builder.toCriteria()));
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{linkId}")
    @Transactional(readOnly=true)
    public DataLinkInterface getLink(@PathParam("linkId") final Integer linkId) {
        readLock();
        try {
            return m_dataLinkInterfaceDao.get(linkId);
        } finally {
            readUnlock();
        }
    }

    @PUT
    @Path("{linkId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateDataLinkInterface(@PathParam("linkId") final Integer linkId, final MultivaluedMapImpl params) {
        writeLock();
        try {
            LOG.debug("updateDataLinkInterface: Updating DataLinkInterface with ID {}", linkId);
            final DataLinkInterface iface = m_dataLinkInterfaceDao.get(linkId);
            if (iface != null) {
                setProperties(params, iface);
                LOG.debug("updateDataLinkInterface: DataLinkInterface with ID {} updated", linkId);
                m_dataLinkInterfaceDao.saveOrUpdate(iface);
                return Response.seeOther(getRedirectUri(m_uriInfo)).build();
            }
            return Response.notModified(linkId.toString()).build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceDataLinkInterface(final DataLinkInterface iface) {
        writeLock();
        try {
            if (iface.getNode() == null && iface.getNodeId() != null) {
                iface.setNode(m_nodeDao.get(iface.getNodeId()));
            }
            if (iface.getSource() == null) {
                iface.setSource("rest");
            }
            LOG.debug("addOrReplaceDataLinkInterface: Adding data link interface {}", iface);
            m_dataLinkInterfaceDao.saveOrUpdate(iface);
            return Response.seeOther(getRedirectUri(m_uriInfo, iface.getId())).build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{linkId}")
    public Response deleteDataLinkInterface(@PathParam("linkId") Integer linkId) {
        writeLock();
        try {
            LOG.debug("deleteDataLinkInterface: deleting DataLinkInterface with ID {}", linkId);
            final DataLinkInterface iface = m_dataLinkInterfaceDao.get(linkId);
            m_dataLinkInterfaceDao.delete(iface);
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

}
