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
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapList;
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
    private static final int LIMIT=10;

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
        OnmsCriteria criteria = getQueryFilters();
        return new OnmsMapList(m_mapDao.findMatching(criteria));
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
    public OnmsMap getMap(@PathParam("mapId") int mapId) {
        return m_mapDao.get(mapId);
    }

    /**
     * <p>addMap</p>
     *
     * @param map a {@link org.opennms.netmgt.model.OnmsMap} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addMap(OnmsMap map) {
        log().debug("addMap: Adding map " + map);
        m_mapDao.save(map);
        return Response.ok(map).build();
    }

    /**
     * <p>deleteMap</p>
     *
     * @param mapId a int.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{mapId}")
    public Response deleteMap(@PathParam("mapId") int mapId) {
        OnmsMap map = m_mapDao.get(mapId);
        if (map == null)
            throwException(Response.Status.BAD_REQUEST, "deleteMap: Can't find map with id " + mapId);
        log().debug("deleteMap: deleting map " + mapId);
        m_mapDao.delete(map);
        return Response.ok().build();
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
    public Response updateMap(@PathParam("mapId") int mapId, MultivaluedMapImpl params) {
        OnmsMap map = m_mapDao.get(mapId);
        if (map == null)
            throwException(Response.Status.BAD_REQUEST, "updateMap: Can't find map with id " + mapId);
        log().debug("updateMap: updating map " + map);
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(map);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                @SuppressWarnings("unchecked")
				Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateMap: map " + map + " updated");
        m_mapDao.saveOrUpdate(map);
        return Response.ok(map).build();
    }

    /**
     * <p>getMapElementResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsMapElementResource} object.
     */
    @Path("{mapId}/mapElements")
    public OnmsMapElementResource getMapElementResource() {
        return m_context.getResource(OnmsMapElementResource.class);
    }

    private OnmsCriteria getQueryFilters() {
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsMap.class);

    	setLimitOffset(params, criteria, LIMIT, false);
    	addOrdering(params, criteria, false);
        // Set default ordering
        addOrdering(
                new MultivaluedMapImpl(
                    new String[][] { 
                        new String[] { "orderBy", "lastModifiedTime" }, 
                        new String[] { "order", "desc" } 
                    }
                ), criteria, false
            );
    	addFiltersToCriteria(params, criteria, OnmsMap.class);

        return getDistinctIdCriteria(OnmsMap.class, criteria);
    }
}
