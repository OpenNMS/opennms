/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.opennms.netmgt.model.OnmsAlarm;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("alarms")
public interface AlarmRestApi {

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Update memo for alarm", description = "Update memo for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);


    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Update journal for alarm", description = "Update journal for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);

    @DELETE
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Remove memo for alarm", description = "Remove memo for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId);

    @DELETE
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Remove journal for alarm", description = "Remove journal for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response removeJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId);


    @POST
    @Path("{id}/ticket/create")
    @Operation(summary = "Create ticket for alarm", description = "Create ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response createTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;

    @POST
    @Path("{id}/ticket/update")
    @Operation(summary = "Update ticket for alarm", description = "Update ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;

    @POST
    @Path("{id}/ticket/close")
    @Operation(summary = "Close ticket for alarm", description = "Close ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response closeTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all types of alaram", description = "Get all types of alaram  ", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get alarams count", description = "Get a count's all types of alarams", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get the properties", description = "Get all types of properties", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm properties not found",
                    content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a properties ", description = "Get a properties  for a specific propertyId", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm property not found",
                    content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get id of alaram", description = "Get id of alaram", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm id not found",
                    content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @POST
    @Path("{id}")
    @Operation(summary = "Create an alaram", description = "Create an alaram by id", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response createSpecific() ;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Create an alaram", description = "Create an alaram", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, AlarmDTO object) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Create many alaram", description = "Create many alaram", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad input request",
                    content = @Content)
    })
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) ;

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(summary = "Update  an alarm", description = "Update an alarm by id", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final Integer id, final OnmsAlarm object);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(summary = "Update  an alarm properties", description = "Update an alaram properties by id", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final MultivaluedMapImpl params) ;

    @DELETE
    @Operation(summary = " Delete Many  alaram", description = "Delete Many  alaram", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarms not found",
                    content = @Content)
    })
    Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete an  alaram", description = "Delete an  alaram by id", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alarm not found",
                    content = @Content)
    })
    Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id) ;


}