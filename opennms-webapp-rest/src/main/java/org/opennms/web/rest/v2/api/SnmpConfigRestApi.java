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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.web.rest.v2.model.SnmpConfigInfoDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("snmp-config")
@Tag(name = "SnmpConfig", description = "SNMP Configuration API")
public interface SnmpConfigRestApi {
    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get SNMP configuration",
            description = "Get SNMP configuration",
            operationId = "getSnmpConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration retrieved successfully",
                    content = @Content)
    })
    Response getSnmpConfig();

    @GET
    @Path("/lookup")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Lookup SNMP configuration",
            description = "Lookup SNMP configuration given an ipAddress and location",
            operationId = "getConfigForIp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration for the given item retrieved successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response getConfigForIp(
            @QueryParam("ipAddress") String ipAddress,
            @QueryParam("location") String location);

    @PUT
    @Path("/definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add an SNMP configuration definition",
            description = "Add an SNMP configuration definition, given an IP address or IP address range, and a location.",
            operationId = "addDefinition"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SNMP configuration definition added successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response addDefinition(SnmpConfigInfoDto dto);

    @DELETE
    @Path("/definition")
    @Operation(
            summary = "Delete an SNMP configuration definition",
            description = "Delete an SNMP configuration definition given an ipAddress and location",
            operationId = "removeDefinition"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration definition for the given item removed successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response removeDefinition(
            @QueryParam("ipAddress") String ipAddress,
            @QueryParam("location") String location);
}
