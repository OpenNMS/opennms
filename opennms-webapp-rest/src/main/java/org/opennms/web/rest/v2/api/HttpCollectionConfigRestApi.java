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
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpCollectionMetadataDto;
import org.opennms.web.rest.v2.model.HttpCollectionSettingsDto;
import org.opennms.web.rest.v2.model.HttpCollectionSummaryDto;

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

@Path("httpcollection")
@Tag(name = "HttpCollection", description = """
        HTTP collector configuration API V2 (the contents of the former `http-datacollection-config.xml`).

        Requires ROLE_ADMIN for every method, reads included, because a URL can carry credentials in its
        user info or in an `Authorization` header.

        An HTTP collection is the unit of transfer: `GET`, `POST` and `PUT` carry the whole collection,
        including its URIs and their attributes. A URI left out of a `PUT` body is deleted.

        `GET` on a collection or on the settings returns an `ETag`. Sending it back as `If-Match` on a write
        makes the write fail with a 412 rather than overwrite a change someone else made in the meantime.
        Omitting `If-Match` keeps the last writer.

        The HTTP collector reads the configuration on every collection, so a change applies from the next
        collection on. A collectd package service selects a collection through its `collection` parameter;
        a collection that is in use cannot be renamed or deleted.""")
public interface HttpCollectionConfigRestApi {

    String COLLECTION_JSON_EXAMPLE = """
            {
              "name": "doc-count",
              "rrd": {
                "step": 300,
                "rras": [ "RRA:AVERAGE:0.5:1:2016", "RRA:AVERAGE:0.5:12:1488" ]
              },
              "uris": [
                {
                  "name": "document-counts",
                  "url": {
                    "method": "GET",
                    "httpVersion": "1.1",
                    "scheme": "http",
                    "host": "${ipaddr}",
                    "port": 80,
                    "path": "/test/resources/httpcolltest.html",
                    "matches": "(?s).*?Document Count:\\\\s+([0-9]+).*",
                    "responseRange": "100-399",
                    "dotall": true,
                    "headers": [ { "name": "Accept", "value": "text/html" } ],
                    "parameters": []
                  },
                  "attributes": [
                    { "alias": "documentCount", "matchGroup": 1, "type": "gauge" }
                  ]
                }
              ]
            }""";

    // ---------------------------------------------------------------- settings

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the HTTP collector settings",
            description = "The settings that apply to every collection. The response carries an `ETag`.",
            operationId = "getHttpCollectionSettings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settings retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = HttpCollectionSettingsDto.class))),
            @ApiResponse(responseCode = "404", description = "No HTTP collector configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getSettings(@Context SecurityContext securityContext);

    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the HTTP collector settings",
            description = "Replace the settings that apply to every collection. The collections are left as they are.",
            operationId = "updateHttpCollectionSettings")
    @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = HttpCollectionSettingsDto.class),
            examples = @ExampleObject(value = "{ \"rrdRepository\": \"/opt/opennms/share/rrd/snmp/\" }")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Settings updated. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No HTTP collector configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored settings.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateSettings(HttpCollectionSettingsDto settings,
            @Parameter(description = "Version as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    // ------------------------------------------------------------- collections

    @GET
    @Path("collections")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List HTTP collections",
            description = "List every HTTP collection without its URIs, in the stored order.",
            operationId = "getHttpCollections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collections retrieved successfully. An empty array when no configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = HttpCollectionSummaryDto.class))))
    })
    Response getCollections(@Context SecurityContext securityContext);

    @GET
    @Path("collections/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get an HTTP collection",
            description = """
        Retrieve one HTTP collection with all of its URIs and attributes. URL attributes the configuration
        leaves unset are returned with their default. The response carries an `ETag`.""",
            operationId = "getHttpCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = HttpCollectionDto.class),
                            examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "HTTP collection 'doc-count' does not exist.")))
    })
    Response getCollection(
            @Parameter(description = "Name of the collection.", example = "doc-count")
            @PathParam("name") String name,
            @Context SecurityContext securityContext);

    @POST
    @Path("collections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create an HTTP collection",
            description = "Create a new HTTP collection. Its name must not be in use, ignoring case, because the collector looks collections up ignoring case.",
            operationId = "createHttpCollection")
    @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = HttpCollectionDto.class),
            examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collection created. The `Location` header points at it."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "HTTP collection 'doc-count': URI at index 0: url path is required."))),
            @ApiResponse(responseCode = "404", description = "No HTTP collector configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A collection of that name already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response createCollection(HttpCollectionDto collection, @Context SecurityContext securityContext);

    @PUT
    @Path("collections/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace an HTTP collection",
            description = """
        Replace an HTTP collection in full. URIs missing from the body are removed. Giving the body a different
        `name` renames the collection, provided the new name is free and no collectd package uses the
        collection.""",
            operationId = "updateHttpCollection")
    @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = HttpCollectionDto.class),
            examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new name is taken, or a collectd package uses the collection that is renamed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored collection; it changed since it was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateCollection(
            @Parameter(description = "Name of the collection to replace.", example = "doc-count")
            @PathParam("name") String name,
            HttpCollectionDto collection,
            @Parameter(description = "Version as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("collections/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete an HTTP collection",
            description = "Delete an HTTP collection. A collection a collectd package service still uses through its `collection` parameter cannot be deleted.",
            operationId = "deleteHttpCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A collectd package still uses the collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "HTTP collection 'doc-count' is used by collectd packages: example1."))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteCollection(
            @Parameter(description = "Name of the collection to delete.", example = "doc-count")
            @PathParam("name") String name,
            @Parameter(description = "Version as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    // ---------------------------------------------------------------- metadata

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the values an HTTP collection editor can choose from",
            description = "Attribute types, HTTP methods and versions, common schemes, and the RRD settings suggested for a new collection.",
            operationId = "getHttpCollectionMetadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = HttpCollectionMetadataDto.class)))
    })
    Response getMetadata(@Context SecurityContext securityContext);

    // ------------------------------------------------------- download / upload

    @GET
    @Path("download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Download the HTTP collector configuration",
            description = """
        Download the whole configuration as JSON or XML. The response is served as an attachment
        (`http-datacollection-config.json` or `http-datacollection-config.xml`) and is accepted back by the
        matching upload endpoint.""",
            operationId = "downloadHttpCollectionConfiguration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "`format` was neither `json` nor `xml`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No HTTP collector configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadConfiguration(
            @Parameter(description = "Serialization format. Defaults to `json`.",
                    example = "xml", schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload the HTTP collector configuration in JSON format",
            description = """
        Replace the whole configuration. The single part must be named `upload` and hold the JSON form that
        `GET /httpcollection/download?format=json` produces. An upload that drops a collection a collectd
        package still uses is refused.""",
            operationId = "uploadHttpCollectionConfiguration")
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the JSON configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The upload drops a collection a collectd package still uses.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadConfiguration(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload the HTTP collector configuration in XML format",
            description = """
        Replace the whole configuration. The single part must be named `upload` and hold an
        `<http-datacollection-config>` document, the form of the former `http-datacollection-config.xml`.
        The document is validated against `http-datacollection-config.xsd`. The same rules as for the JSON
        upload apply.""",
            operationId = "uploadHttpCollectionConfigurationXml")
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the XML configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable or schema-invalid XML, or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The upload drops a collection a collectd package still uses.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadConfigurationXml(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);
}
