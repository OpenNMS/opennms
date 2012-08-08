/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterfaceList;
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
    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    @Context
    UriInfo m_uriInfo;

    @Context
    ResourceContext m_context;

    /**
     * <p>getLinks</p>
     *
     * @return a {@link org.opennms.netmgt.model.DataLinkInterfaceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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

    /**
     * <p>getLink</p>
     *
     * @param mapId a int.
     * @return a {@link org.opennms.netmgt.model.OnmsMap} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{linkId}")
    public DataLinkInterface getLink(@PathParam("linkId") final int linkId) {
        readLock();
        try {
            return m_dataLinkInterfaceDao.get(linkId);
        } finally {
            readUnlock();
        }
    }

}
