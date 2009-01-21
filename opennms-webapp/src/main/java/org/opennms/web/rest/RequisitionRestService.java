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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.RequisitionDao;
import org.opennms.netmgt.model.OnmsRequisitionCollection;
import org.opennms.netmgt.provision.persist.OnmsRequisition;
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
    private RequisitionDao m_requisitionDao;
    
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
        return m_requisitionDao.get(foreignSource);
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
        return Integer.toString(m_requisitionDao.countAll());
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
        return new OnmsRequisitionCollection(m_requisitionDao.findAll());
    }

    /**
     * Updates the requisition with foreign source "foreignSource" 
     */
/*    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public void updateRequisition(@PathParam("foreignSource") String foreignSource) {
        OnmsRequisition req = m_requisitionDao.get(foreignSource);
        OnmsEvent event = m_eventDao.get(new Integer(eventId));
        if (ack == null) {
            throw new IllegalArgumentException(
                    "Must supply the 'ack' parameter, set to either 'true' or 'false'");
        }
        processEventAck(event, ack);
    }*/

    /**
     * Updates all the events that match any filter/query supplied in the form. 
     * If the "ack" parameter is "true", then acks the events as the current logged in user, otherwise unacks the events
     * 
     * @param formProperties Map of the parameters passed in by form encoding
     */
/*    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public void updateEvents(MultivaluedMapImpl formProperties) {
        Boolean ack=false;
        if(formProperties.containsKey("ack")) {
            ack="true".equals(formProperties.getFirst("ack"));
            formProperties.remove("ack");
        }
        
        OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
        setLimitOffset(formProperties, criteria, 10);
        addFiltersToCriteria(formProperties, criteria, OnmsEvent.class);

        
        for (OnmsEvent event : m_eventDao.findMatching(criteria)) {
            processEventAck(event, ack);
        }
    }*/
}
