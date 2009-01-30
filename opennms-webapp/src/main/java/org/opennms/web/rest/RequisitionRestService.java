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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.model.OnmsRequisitionCollection;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.OnmsRequisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("requisitions")
public class RequisitionRestService extends OnmsRestService {
    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository;
    
    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}")
    @Transactional
    public OnmsRequisition getRequisition(@PathParam("foreignSource") String foreignSource) {
        return m_foreignSourceRepository.getRequisition(foreignSource);
    }

    /**
     * returns a plaintext string being the number of requisitions
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_foreignSourceRepository.getRequisitions().size());
    }

    /**
     * Returns all the requisitions
     * 
     * @return Collection of OnmsRequisitions (ready to be XML-ified)
     * @throws ParseException
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional
    public OnmsRequisitionCollection getRequisitions() throws ParseException {
        return new OnmsRequisitionCollection(m_foreignSourceRepository.getRequisitions());
    }

    /**
     * Updates the requisition with foreign source "foreignSource" 
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addRequisition(OnmsRequisition requisition) {
        log().debug("addRequisition: Adding requisition " + requisition.getForeignSource());
        m_foreignSourceRepository.save(requisition);
        return Response.ok(requisition).build();
    }

}
