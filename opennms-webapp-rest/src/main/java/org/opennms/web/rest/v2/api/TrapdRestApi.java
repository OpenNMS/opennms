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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;

@Path("trapd")
@Tag(name = "Trapd", description = "Trapd API V2")
public interface TrapdRestApi {

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload trapd configuration",
            description = "Upload trapd-configuration XML and persist it to DB.",
            operationId = "uploadTrapdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid trapd XML or missing upload field"),
            @ApiResponse(responseCode = "500", description = "Failed to persist trapd configuration")
    })
    Response uploadTrapdConfiguration(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);
    
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
    Response updateTrapdConfiguration(TrapdConfiguration payload, @Context SecurityContext securityContext);

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Save SNMPv3 user",
            description = "Save SNMPv3 user configuration with provided JSON payload.",
            operationId = "saveTrapdUser"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user payload"),
            @ApiResponse(responseCode = "409", description = "SNMPv3 user with provided securityName already exists"),
            @ApiResponse(responseCode = "500", description = "Failed to save user")
    })
    Response saveTrapdUser(Snmpv3User user, @Context SecurityContext securityContext);

    @PUT
    @Path("user/{securityName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update SNMPv3 user",
            description = "Update SNMPv3 user configuration for the provided securityName.",
            operationId = "updateTrapdUser"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user payload"),
            @ApiResponse(responseCode = "404", description = "SNMPv3 user with provided securityName was not found"),
            @ApiResponse(responseCode = "500", description = "Failed to update user")
    })
    Response updateTrapdUser(@PathParam("securityName") String securityName, Snmpv3User user, @Context SecurityContext securityContext);

    @DELETE
    @Path("user/{securityName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete SNMPv3 user",
            description = "Delete SNMPv3 user configuration with provided securityName.",
            operationId = "deleteTrapdUser"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid securityName"),
            @ApiResponse(responseCode = "404", description = "SNMPv3 user with provided securityName was not found"),
            @ApiResponse(responseCode = "500", description = "Failed to delete user")
    })
    Response deleteTrapdUser(@PathParam("securityName") String securityName, @Context SecurityContext securityContext);
}

