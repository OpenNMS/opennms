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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpProfile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

    @POST
    @Path("/defaults")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Save SNMP default override configuration",
            description = "Save SNMP default override configuration",
            operationId = "saveDefaultOverrides"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration default overrides saved successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error or Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response saveDefaultOverrides(Configuration config);

    @PUT
    @Path("/definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add an SNMP configuration definition",
            description = "Add an SNMP configuration definition.",
            operationId = "addDefinition"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SNMP configuration definition added successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error or Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response addDefinition(Definition definition);

    @DELETE
    @Path("/definition")
    @Operation(
            summary = "Delete an SNMP configuration definition",
            description = "Delete an SNMP configuration definition for a location. User can provide comma-separated lists of specific IPs or IP ranges or IP match expressions to delete.",
            operationId = "removeDefinition"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration definition for the given item removed successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters, or no matching definition found",
                    content = @Content)
    })
    Response removeDefinition(
            @QueryParam("specifics") String specifics,
            @QueryParam("ranges") String ranges,
            @QueryParam("ipmatches") String ipMatches,
            @QueryParam("location") String location);

    @POST
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add or update an SNMP configuration profile",
            description = "Add or update an SNMP configuration profile.",
            operationId = "saveProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration profile added or updated successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response saveProfile(SnmpProfile profile);

    @DELETE
    @Path("/profile")
    @Operation(
            summary = "Delete an SNMP configuration profile",
            description = "Delete an SNMP configuration profile with the given label.",
            operationId = "removeProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration profile with the given label removed successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found – profile with given label not found",
                    content = @Content)
    })
    Response removeProfile(@QueryParam("label") final String label);

    @GET
    @Path("/download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download SNMP configuration",
            description = "Download SNMP configuration in Json or XML format, suitable for upload.",
            operationId = "downloadConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration in download format retrieved successfully",
                    content = { @Content(mediaType = "application/json"), @Content(mediaType = "application/xml") }),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content)
    })
    Response downloadConfig(@QueryParam("format") String format);

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload SNMP configuration in Json format.",
            description = "Upload SNMP configuration in Json format and apply the changes.",
            operationId = "uploadConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration uploaded and applied successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing configuration.",
                    content = @Content)
    })
    Response uploadConfig(@Multipart("upload") Attachment attachment);

    @POST
    @Path("/upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload SNMP configuration in XML format",
            description = "Upload SNMP configuration in XML format and apply the changes.",
            operationId = "uploadConfigXml"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration uploaded and applied successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing configuration.",
                    content = @Content)
    })
    Response uploadConfigXml(@Multipart("upload") Attachment attachment);
}
