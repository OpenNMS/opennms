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
import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>OnmsMapRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Path("maps")
@Transactional
public class OnmsMapRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsMapRestService.class);

    @Autowired
    private OnmsMapDao m_mapDao;

    @Context
    UriInfo m_uriInfo;

    @Context
    ResourceContext m_context;

    /**
     * <p>getMaps</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMapList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsMapList getMaps() {
        readLock();
        
        try {
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsMap.class);
            applyQueryFilters(m_uriInfo.getQueryParameters(), builder);
            builder.orderBy("lastModifiedTime").desc();
            return new OnmsMapList(m_mapDao.findMatching(builder.toCriteria()));
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getMap</p>
     *
     * @param mapId a int.
     * @return a {@link org.opennms.netmgt.model.OnmsMap} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{mapId}")
    public OnmsMap getMap(@PathParam("mapId") final int mapId) {
        readLock();
        try {
            return m_mapDao.get(mapId);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>addMap</p>
     *
     * @param map a {@link org.opennms.netmgt.model.OnmsMap} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addMap(final OnmsMap map) {
        writeLock();
        try {
            LOG.debug("addMap: Adding map {}", map);
            m_mapDao.save(map);
            return Response.seeOther(m_uriInfo.getBaseUriBuilder().path(this.getClass()).path(this.getClass(), "getMap").build(map.getId())).build();
            // return Response.ok(map).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteMap</p>
     *
     * @param mapId a int.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{mapId}")
    public Response deleteMap(@PathParam("mapId") final int mapId) {
        writeLock();
        try {
            final OnmsMap map = m_mapDao.get(mapId);
            if (map == null) throw getException(Response.Status.BAD_REQUEST, "deleteMap: Can't find map with id " + mapId);
            LOG.debug("deleteMap: deleting map {}", mapId);
            m_mapDao.delete(map);
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>updateMap</p>
     *
     * @param mapId a int.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{mapId}")
    public Response updateMap(@PathParam("mapId") final int mapId, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsMap map = m_mapDao.get(mapId);
            if (map == null) throw getException(Response.Status.BAD_REQUEST, "updateMap: Can't find map with id " + mapId);
    
            LOG.debug("updateMap: updating map {}", map);
    
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(map);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
    
            LOG.debug("updateMap: map {} updated", map);
            m_mapDao.saveOrUpdate(map);
            return Response.seeOther(m_uriInfo.getBaseUriBuilder().path(this.getClass()).path(this.getClass(), "getMap").build(mapId)).build();
            // return Response.ok(map).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>getMapElementResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsMapElementResource} object.
     */
    @Path("{mapId}/mapElements")
    public OnmsMapElementResource getMapElementResource() {
        readLock();
        try {
            return m_context.getResource(OnmsMapElementResource.class);
        } finally {
            readUnlock();
        }
    }
}
