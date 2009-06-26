//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.rest;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMapList;
import org.opennms.netmgt.model.OnmsMap;
import com.sun.jersey.spi.resource.PerRequest;
import com.sun.jersey.api.core.ResourceContext;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Component
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

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsMapList getMaps() {
        OnmsCriteria criteria = getQueryFilters();
        return new OnmsMapList(m_mapDao.findMatching(criteria));
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{mapId}")
    public OnmsMap getMap(@PathParam("mapId") int mapId) {
        return m_mapDao.get(mapId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addMap(OnmsMap map) {
        log().debug("addMap: Adding map " + map);
        m_mapDao.save(map);
        return Response.ok(map).build();
    }

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

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{mapId}")
    public Response updateMap(@PathParam("mapId") int mapId, MultivaluedMapImpl params) {
        OnmsMap map = m_mapDao.get(mapId);
        if (map == null)
            throwException(Response.Status.BAD_REQUEST, "updateMap: Can't find map with id " + mapId);
        log().debug("updateMap: updating map " + map);
        BeanWrapper wrapper = new BeanWrapperImpl(map);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateMap: map " + map + " updated");
        m_mapDao.saveOrUpdate(map);
        return Response.ok(map).build();
    }

    @Path("{mapId}/mapElements")
    public OnmsMapElementResource getMapElementResource() {
        return m_context.getResource(OnmsMapElementResource.class);
    }

    private OnmsCriteria getQueryFilters() {
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsMap.class);

    	setLimitOffset(params, criteria, LIMIT);
    	addFiltersToCriteria(params, criteria, OnmsMap.class);

        return criteria;
    }
}
