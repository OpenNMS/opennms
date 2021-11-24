/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.rest;


import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/alarms")
public interface AlarmRestService {

    @GET
    @Path("list") // curl -X GET http://localhost:8181/cxf/alarmservice/alarms/list
    @Produces(MediaType.APPLICATION_JSON)
    Response getAlarms(@Context final SecurityContext securityContext, final UriInfo uriInfo);

    // OnmsAlarm get(Long id);  v1??


    @PUT
    @Path("{id}/memo") // curl -X PUT http://localhost:8181/cxf/alarmservice/alarms/1/memo -d 'user=mark&body=not null'
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);


    @PUT
    @Path("{id}/journal") // curl -X PUT http://localhost:8181/cxf/alarmservice/alarms/1/journal -d 'user=mark&body=not null'
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);

/*
    @POST
    @Path("{id}/ticket/update") // curl -X PUT http://localhost:8181/cxf/alarmservice/alarms/1/ticket/update
    public Response updateTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;

    @POST
    @Path("{id}/ticket/close") // curl -X PUT http://localhost:8181/cxf/alarmservice/alarms/1/ticket/close
    public Response closeTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;
*/

    @DELETE
    @Path("{id}/memo") // curl -X DELETE http://localhost:8181/cxf/alarmservice/alarms/1/memo -d 'user=mark&body=not null'
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId);




    @GET
    @Path("/testjaxrs") // curl -u karaf:karaf -X GET http://localhost:8181/cxf/alarmservice/alarms/testjaxrs
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    Response echoJaxrsSecCtxAnyone(@Context SecurityContext jaxrsSecCtx);


    @GET
    @Path("/testjaas/groomer") // curl -u karaf:karaf -X GET http://localhost:8181/cxf/alarmservice/alarms/testjaas/groomer
    @RolesAllowed("groomer")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    Response echoJaasUserPrincipalGroomer();

    @GET
    @Path("/testjaas/admin") // curl -u karaf:karaf -X GET http://localhost:8181/cxf/alarmservice/alarms/testjaas/admin
    @RolesAllowed("admin")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    Response echoJaasUserPrincipalAdmin();



}
