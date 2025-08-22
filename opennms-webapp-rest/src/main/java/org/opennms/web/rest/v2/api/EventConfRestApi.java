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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
