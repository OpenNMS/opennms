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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdPackageSummaryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("threshd")
@Tag(name = "Threshd", description = """
        Threshd daemon configuration API V2 (the contents of the former `threshd-configuration.xml`).

        A package is the unit of transfer: `GET`, `POST` and `PUT` carry the whole package, including its
        address ranges and its services. A service has no endpoint of its own; it is a field of its package.

        A package binds interfaces to a threshold group through a service parameter named
        `thresholding-group`, whose value is a group name from the thresholding API.

        Scheduled outages attached to a package are also maintained by the scheduled outages API. Both write
        the same document under the same lock, but a client holding a package it read earlier will overwrite
        outage assignments made in the meantime, so re-read before writing.

        The schema requires at least one package, so the last one cannot be deleted.""")
public interface ThreshdConfigRestApi {

    String PACKAGE_JSON_EXAMPLE = """
            {
              "name": "example1",
              "filter": "IPADDR != '0.0.0.0'",
              "specifics": [],
              "includeRanges": [ { "begin": "1.1.1.1", "end": "254.254.254.254" } ],
              "excludeRanges": [],
              "includeUrls": [],
              "services": [
                {
                  "name": "SNMP",
                  "interval": 300000,
                  "userDefined": false,
                  "status": "on",
                  "parameters": [ { "key": "thresholding-group", "value": "mib2" } ]
                }
              ],
              "outageCalendars": []
            }""";

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the threshd configuration",
            description = """
        Retrieve the whole threshd configuration. The response carries an `ETag` to be used as `If-Match`
        on a later write.""",
            operationId = "getThreshdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ThreshdConfigDto.class))),
            @ApiResponse(responseCode = "404", description = "No threshd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Threshd configuration not found.")))
    })
    Response getThreshdConfiguration(@Context SecurityContext securityContext);

    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Replace the threshd configuration",
            description = """
        Replace the whole threshd configuration. Requires ROLE_ADMIN. The body is not a patch: packages
        missing from it are removed. At least one package is required.""",
            operationId = "updateThreshdConfiguration"
    )
    @RequestBody(required = true, description = "The complete threshd configuration.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThreshdConfigDto.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No threshd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateThreshdConfiguration(
            ThreshdConfigDto config,
            @Parameter(description = "Version of the configuration as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @GET
    @Path("packages")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List threshd packages",
            description = "List every threshd package without its address and service detail. Sorted by name.",
            operationId = "getThreshdPackages"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = ThreshdPackageSummaryDto.class))))
    })
    Response getThreshdPackages(@Context SecurityContext securityContext);

    @GET
    @Path("packages/{packageName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a threshd package",
            description = "Retrieve one threshd package with its filter, address ranges and services.",
            operationId = "getThreshdPackage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ThreshdPackageDto.class),
                            examples = @ExampleObject(value = PACKAGE_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No package of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getThreshdPackage(
            @Parameter(description = "Name of the package.", example = "example1")
            @PathParam("packageName") String packageName,
            @Context SecurityContext securityContext);

    @POST
    @Path("packages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a threshd package",
            description = "Create a new threshd package. Requires ROLE_ADMIN. The package name must not already be in use.",
            operationId = "createThreshdPackage"
    )
    @RequestBody(required = true, description = "The package to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThreshdPackageDto.class),
                    examples = @ExampleObject(value = PACKAGE_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Package created. The `Location` header points at the new package."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No threshd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A package of that name already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response createThreshdPackage(ThreshdPackageDto pkg, @Context SecurityContext securityContext);

    @PUT
    @Path("packages/{packageName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Replace a threshd package",
            description = """
        Replace a threshd package in full. Requires ROLE_ADMIN. The body is not a patch: services, ranges and
        outage calendars missing from it are removed — including outage calendars attached through the
        scheduled outages API. Giving the body a different `name` renames the package.""",
            operationId = "updateThreshdPackage"
    )
    @RequestBody(required = true, description = "The complete package.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThreshdPackageDto.class),
                    examples = @ExampleObject(value = PACKAGE_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Package replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No package of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new name is already taken by another package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateThreshdPackage(
            @Parameter(description = "Name of the package to replace.", example = "example1")
            @PathParam("packageName") String packageName,
            ThreshdPackageDto pkg,
            @Parameter(description = "Version of the package as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("packages/{packageName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete a threshd package",
            description = """
        Delete a threshd package. Requires ROLE_ADMIN. The schema requires at least one package, so deleting
        the last one is rejected.""",
            operationId = "deleteThreshdPackage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Package deleted. No response body."),
            @ApiResponse(responseCode = "400", description = "The package is the last one and cannot be deleted.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one threshd package is required; 'example1' cannot be deleted."))),
            @ApiResponse(responseCode = "404", description = "No package of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteThreshdPackage(
            @Parameter(description = "Name of the package to delete.", example = "example1")
            @PathParam("packageName") String packageName,
            @Parameter(description = "Version of the package as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @POST
    @Path("reload")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Ask threshd to reload its daemon configuration",
            description = """
        Send a `reloadDaemonConfig` event for `threshd-configuration.xml`. Requires ROLE_ADMIN. The event is
        asynchronous: a 202 means it was sent, not that threshd has finished reloading.""",
            operationId = "reloadThreshdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Reload event sent."),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The event could not be sent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response reloadThreshdConfiguration(@Context SecurityContext securityContext);

    @GET
    @Path("download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download the threshd configuration",
            description = """
        Download the stored threshd configuration as JSON or XML. Requires ROLE_ADMIN. The response is served
        as an attachment (`threshd-configuration.json` or `threshd-configuration.xml`) and is accepted back
        by the matching upload endpoint.""",
            operationId = "downloadThreshdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "`format` was neither `json` nor `xml`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No threshd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The stored configuration could not be serialized.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadThreshdConfiguration(
            @Parameter(description = "Serialization format. Defaults to `json`.",
                    example = "xml", schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload the threshd configuration in JSON format",
            description = """
        Replace the whole stored threshd configuration. Requires ROLE_ADMIN. The single part must be named
        `upload` and hold a JSON document of the same shape as `GET /threshd/config`.""",
            operationId = "uploadThreshdConfiguration"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the JSON threshd configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Failed to persist the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadThreshdConfiguration(@Multipart("upload") Attachment attachment,
                                        @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload the threshd configuration in XML format",
            description = """
        Replace the whole stored threshd configuration. Requires ROLE_ADMIN. The single part must be named
        `upload` and hold a `<threshd-configuration>` document, validated against `thresholding.xsd` before
        it is stored.""",
            operationId = "uploadThreshdConfigurationXml"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the XML threshd configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable or schema-invalid XML.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Failed to persist the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadThreshdConfigurationXml(@Multipart("upload") Attachment attachment,
                                           @Context SecurityContext securityContext);
}
