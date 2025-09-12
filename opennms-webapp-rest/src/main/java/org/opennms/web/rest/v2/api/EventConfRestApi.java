/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements. See the LICENSE.md file
 * distributed with this work for additional information.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License"); you may not use
 * this file except in compliance with the License.
 * https://www.gnu.org/licenses/agpl-3.0.txt
 */
package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.EventConfEventPayload;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;


import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PATCH;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("eventconf")
@Tag(name = "EventConf", description = "EventConf API")
public interface EventConfRestApi {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Operation(
            summary = "Upload eventconf files",
            description = "Upload one or more eventconf files including optional eventconf.xml to determine file order.",
            operationId = "uploadEventConfFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload successful"),
            @ApiResponse(responseCode = "400", description = "Invalid eventconf.xml or request")
    })
    Response uploadEventConfFiles(@Multipart("upload") List<Attachment> attachments,
                                  @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("filter")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Filter EventConf Records",
            description = "Fetch EventConf records based on provided filters such as UEI, vendor, source and name.",
            operationId = "filterEventConf"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConf records retrieved successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No matching EventConf records found for the given criteria",
                    content = @Content)
    })
    Response filterEventConf(
            @QueryParam("uei") String uei,
            @QueryParam("vendor") String vendor,
            @QueryParam("sourceName") String sourceName,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @Context SecurityContext securityContext );

    @PATCH
    @Path("/sources/status")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable EventConf Sources",
            description = "Enable or disable one or more sources (and optionally cascade to their events)",
            operationId = "enableDisableEventConfSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    Response enableDisableEventConfSources(EventConfSrcEnableDisablePayload eventConfSrcEnableDisablePayload, @Context SecurityContext securityContext) throws Exception;

    @DELETE
    @Path("/sources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Operation(
            summary = "Delete EventConf Sources",
            description = "Delete one or more eventConf sources by their IDs.",
            operationId = "deleteEventConfSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid IDs)"),
            @ApiResponse(responseCode = "404", description = "One or more sources not found")
    })
    Response deleteEventConfSources(EventConfSourceDeletePayload eventConfSourceDeletePayload,
                                    @Context SecurityContext securityContext) throws Exception;

    @PATCH
    @Path("/sources/{sourceId}/events/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Operation(
            summary = "Update EventConf Sources events",
            description = "Update one or more eventConf sources  by their events IDs.",
            operationId = "enableDisableConfSourcesEvents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid IDs)"),
            @ApiResponse(responseCode = "404", description = "One or more sources not found")
    })
    Response enableDisableEventConfSourcesEvents(@PathParam("sourceId") final Long sourceId,EnableDisableConfSourceEventsPayload enableDisableConfSourceEventsPayload,
                                    @Context SecurityContext securityContext) throws Exception;

    @PUT
    @Path("/sources/events/{eventId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Operation(
            summary = "Update EventConf  Event",
            description = "Update  eventConf event by sourceId and eventId.",
            operationId = "updateEventConfEvent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "One or more sources not found")
    })
    Response updateEventConfEvent(@PathParam("eventId") final Long eventId, EventConfEventPayload payload,
                                  @Context SecurityContext securityContext) throws Exception;
}
