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
// Modifications:
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

import java.text.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.ForeignSourceDao;
import org.opennms.netmgt.model.OnmsForeignSourceCollection;
import org.opennms.netmgt.provision.persist.OnmsForeignSource;
import org.opennms.netmgt.provision.persist.StringIntervalAdapter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("foreignSources")
public class ForeignSourceRestService extends OnmsRestService {
    @Autowired
    private ForeignSourceDao m_foreignSourceDao;
    
    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{name}")
    @Transactional
    public OnmsForeignSource getForeignSource(@PathParam("name") String foreignSource) {
        return m_foreignSourceDao.get(foreignSource);
    }

    /**
     * returns a plaintext string being the number of foreign sources
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_foreignSourceDao.countAll());
    }

    /**
     * Returns all the foreign sources
     * 
     * @return Collection of OnmsForeignSources (ready to be XML-ified)
     * @throws ParseException
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional
    public OnmsForeignSourceCollection getForeignSources() throws ParseException {
        return new OnmsForeignSourceCollection(m_foreignSourceDao.findAll());
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addForeignSource(OnmsForeignSource foreignSource) {
        log().debug("addForeignSource: Adding foreignSource " + foreignSource.getName());
        m_foreignSourceDao.save(foreignSource);
        return Response.ok(foreignSource).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{name}")
    public Response updateForeignSource(@PathParam("name") String foreignSource, MultivaluedMapImpl params) {
        OnmsForeignSource fs = m_foreignSourceDao.get(foreignSource);
        if (fs == null) {
            throwException(Status.BAD_REQUEST, "updateForeignSource: Can't find foreign source with name " + foreignSource);
        }
        log().debug("updateForeignSource: updating foreign source " + foreignSource);
        BeanWrapper wrapper = new BeanWrapperImpl(fs);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                Object value = null;
                String stringValue = params.getFirst(key);
                if (key.equals("scanInterval")) {
                    StringIntervalAdapter sia = new StringIntervalAdapter();
                    value = sia.unmarshal(stringValue);
                } else {
                    value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                }
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateForeignSource: foreign source " + foreignSource + " updated");
        m_foreignSourceDao.save(fs);
        return Response.ok(fs).build();
    }
    
    @DELETE
    @Path("{name}")
    public Response deleteForeignSource(@PathParam("name") String foreignSource) {
        OnmsForeignSource fs = m_foreignSourceDao.get(foreignSource);
        if (fs == null) {
            throwException(Status.BAD_REQUEST, "deleteForeignSource: Can't find foreign source with name " + foreignSource);
        }
        log().debug("deleteForeignSource: deleting foreign source " + foreignSource);
        m_foreignSourceDao.delete(fs);
        return Response.ok().build();
    }


}
