/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.annotations.media.Content;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.web.rest.v2.model.TrapdConfigDto;

@Path("trapd")
@Tag(name = "Trapd", description = "Trapd API V2")
public interface TrapdRestApi {
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload Trap configuration in JSON format.",
            description = "Upload Trap configuration in JSON format and apply the changes.",
            operationId = "uploadTrapdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trap configuration uploaded and applied successfully",
                content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing configuration.",
                content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to persist Trap configuration.",
                content = @Content)
    })
    Response uploadTrapdConfiguration(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload Trap configuration in XML format.",
            description = "Upload Trap configuration in XML format and apply the changes.",
            operationId = "uploadTrapdConfigurationXml"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trap configuration uploaded and applied successfully",
                content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing configuration.",
                content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to persist Trap configuration.",
                content = @Content)
    })
    Response uploadTrapdConfigurationXml(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @GET
    @Path("download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download trapd configuration",
            description = "Download trapd configuration in JSON or XML format.",
            operationId = "downloadConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trapd configuration in download format retrieved successfully",
                    content = { @Content(mediaType = "application/json"), @Content(mediaType = "application/xml") }),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response downloadTrapdConfig(@QueryParam("format") String format);

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get trapd configuration",
            description = "Retrieve the current trapd configuration.",
            operationId = "getTrapdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Configuration not found"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve trapd configuration")
    })
    Response getTrapdConfiguration(@Context SecurityContext securityContext);
    
    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update trapd configuration",
            description = "Update trapd configuration with provided JSON payload.",
            operationId = "updateTrapdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid configuration payload"),
            @ApiResponse(responseCode = "500", description = "Failed to update trapd configuration")
    })
    Response updateTrapdConfiguration(TrapdConfigDto payload, @Context SecurityContext securityContext);
}
