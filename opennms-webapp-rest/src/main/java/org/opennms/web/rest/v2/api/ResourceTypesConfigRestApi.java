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
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigSummaryDto;
import org.opennms.web.rest.v2.model.ResourceTypesMetadataDto;

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

@Path("resource-types")
@Tag(name = "ResourceTypes", description = """
        Resource types configuration API V2 (the contents of the former `etc/resource-types.d/` directory).

        Resource types are kept in configurations, one per former file, e.g. `postgresql-resource`. A
        configuration is the unit of transfer: `GET`, `POST` and `PUT` all carry the whole configuration with
        all of its resource types. A resource type left out of a `PUT` body is therefore deleted.

        A resource type name must be unique across all configurations. Resource types defined in
        `datacollection-config.xml` and provided by extensions are not managed here: a stored resource type
        overrides a data collection one of the same name, and an extension's resource type overrides a stored
        one. `GET metadata` names both, so a client can warn about it.

        Deleting or renaming a resource type is not checked against its users: data collection groups and
        thresholds that reference it stop working.

        `GET` on a configuration returns an `ETag`. Sending it back as `If-Match` on a write makes the write
        fail with a 412 rather than overwrite a change someone else made in the meantime. Omitting `If-Match`
        keeps the last writer.""")
public interface ResourceTypesConfigRestApi {

    String CONFIG_JSON_EXAMPLE = """
            {
              "id": "postgresql-resource",
              "resourceTypes": [
                {
                  "name": "pgTableSpace",
                  "label": "PostgreSQL Tablespace",
                  "resourceLabel": "${spcname}",
                  "persistenceSelectorStrategy": {
                    "className": "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
                    "parameters": []
                  },
                  "storageStrategy": {
                    "className": "org.opennms.netmgt.collection.support.IndexStorageStrategy",
                    "parameters": []
                  }
                }
              ]
            }""";

    @GET
    @Path("configs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List resource types configurations",
            description = "List every stored resource types configuration with the names of its resource types, sorted by id.",
            operationId = "getResourceTypesConfigs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurations retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = ResourceTypesConfigSummaryDto.class)))),
            @ApiResponse(responseCode = "500", description = "Failed to read the resource types configurations.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getConfigs(@Context SecurityContext securityContext);

    @GET
    @Path("configs/{configId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a resource types configuration",
            description = """
        Retrieve one configuration with all of its resource types. The response carries an `ETag` to be used
        as `If-Match` on a later write.""",
            operationId = "getResourceTypesConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ResourceTypesConfigDto.class),
                            examples = @ExampleObject(value = CONFIG_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No configuration with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource types configuration 'postgresql-resource' does not exist.")))
    })
    Response getConfig(
            @Parameter(description = "Id of the configuration.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            @Context SecurityContext securityContext);

    @POST
    @Path("configs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a resource types configuration",
            description = "Create a new configuration. Requires ROLE_ADMIN. The id must not already be in use.",
            operationId = "createResourceTypesConfig"
    )
    @RequestBody(required = true, description = "The configuration to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResourceTypesConfigDto.class),
                    examples = @ExampleObject(value = CONFIG_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Configuration created. The `Location` header points at it."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid resource type at index 0: the label of 'pgTableSpace' is required."))),
            @ApiResponse(responseCode = "409", description = "The id is taken, or a resource type name is already defined by another configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource type 'pgTableSpace' is already defined in configuration 'postgresql-resource'.")))
    })
    Response createConfig(ResourceTypesConfigDto config, @Context SecurityContext securityContext);

    @PUT
    @Path("configs/{configId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Replace a resource types configuration",
            description = """
        Replace a configuration in full. Requires ROLE_ADMIN. The body is not a patch: resource types missing
        from it are removed. Giving the body a different `id` renames the configuration, provided the new id
        is free.""",
            operationId = "updateResourceTypesConfig"
    )
    @RequestBody(required = true, description = "The complete configuration.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResourceTypesConfigDto.class),
                    examples = @ExampleObject(value = CONFIG_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No configuration with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new id is taken, or a resource type name is already defined by another configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored configuration; it changed since it was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource types configuration 'postgresql-resource' has changed since it was read.")))
    })
    Response updateConfig(
            @Parameter(description = "Id of the configuration to replace.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            ResourceTypesConfigDto config,
            @Parameter(description = "Version of the configuration as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("configs/{configId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete a resource types configuration",
            description = """
        Delete a configuration with all of its resource types. Requires ROLE_ADMIN. Data collection groups and
        thresholds that use these resource types stop working.""",
            operationId = "deleteResourceTypesConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No configuration with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteConfig(
            @Parameter(description = "Id of the configuration to delete.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            @Parameter(description = "Version of the configuration as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the values a resource type editor can choose from",
            description = """
        The strategy classes shipped with OpenNMS, and the names of the resource types defined outside of the
        stored configurations: in `datacollection-config.xml`, overridden by a stored type of the same name,
        and by extensions, which override a stored type of the same name.""",
            operationId = "getResourceTypesMetadata"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ResourceTypesMetadataDto.class)))
    })
    Response getMetadata(@Context SecurityContext securityContext);

    @GET
    @Path("configs/{configId}/download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download a resource types configuration",
            description = """
        Download a configuration as JSON or XML. Requires ROLE_ADMIN. The response is served as an attachment
        named after the configuration. The XML is a `<resource-types>` document, the format of the former
        files in `etc/resource-types.d/`. Both are accepted back by the matching upload endpoint.""",
            operationId = "downloadResourceTypesConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "`format` was neither `json` nor `xml`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No configuration with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadConfig(
            @Parameter(description = "Id of the configuration.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            @Parameter(description = "Serialization format. Defaults to `json`.",
                    example = "xml", schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext);

    @POST
    @Path("configs/{configId}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload a resource types configuration in JSON format",
            description = """
        Create the configuration, or replace it in full if it exists. Requires ROLE_ADMIN. The single part
        must be named `upload` and hold a JSON document of the same shape as the JSON download; its `id` is
        ignored, the path names the configuration.""",
            operationId = "uploadResourceTypesConfig"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the JSON configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A resource type name is already defined by another configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadConfig(
            @Parameter(description = "Id of the configuration to create or replace.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            @Multipart("upload") Attachment attachment,
            @Context SecurityContext securityContext);

    @POST
    @Path("configs/{configId}/upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload a resource types configuration in XML format",
            description = """
        Create the configuration, or replace it in full if it exists. Requires ROLE_ADMIN. The single part
        must be named `upload` and hold a `<resource-types>` document, the format of the former files in
        `etc/resource-types.d/`. The same rules as for the JSON upload apply.""",
            operationId = "uploadResourceTypesConfigXml"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the XML configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable or schema-invalid XML, or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A resource type name is already defined by another configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadConfigXml(
            @Parameter(description = "Id of the configuration to create or replace.", example = "postgresql-resource")
            @PathParam("configId") String configId,
            @Multipart("upload") Attachment attachment,
            @Context SecurityContext securityContext);
}
