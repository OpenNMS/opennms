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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Path("alarms")
public interface AlarmRestApi {

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Update Memo for alarm", description = "Update Memos for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",
                    content = @Content)
    })
    Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);


    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Update Journal for alarm", description = "Update Journal for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",
                    content = @Content)
    })
    Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params);

    @DELETE
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Remove Memo for alarm", description = "Remove Memo for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
            @ApiResponse(responseCode = "404", description = "Node not found",
                    content = @Content)
    })
    Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId);

    @DELETE
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Remove Journal for alarm", description = "Remove Journal for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",
                    content = @Content)
    })
    Response removeJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId);


    @POST
    @Path("{id}/ticket/create")
    @Operation(summary = "Create Ticket for alarm", description = "Create Ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content)
    })
    Response createTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;

    @POST
    @Path("{id}/ticket/update")
    @Operation(summary = "Update Ticket for alarm", description = "Update Ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated Access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content)
    })
    Response updateTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;

    @POST
    @Path("{id}/ticket/close")
    @Operation(summary = "Close Ticket for alarm", description = "Close Ticket for alarm service", tags = {"Alarms"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated Access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content)
    })
    Response closeTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception;


}
