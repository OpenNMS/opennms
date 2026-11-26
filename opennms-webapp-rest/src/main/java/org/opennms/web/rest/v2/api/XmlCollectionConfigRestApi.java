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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.web.rest.v2.model.XmlCollectionConfigDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlCollectionMetadataDto;
import org.opennms.web.rest.v2.model.XmlCollectionSettingsDto;
import org.opennms.web.rest.v2.model.XmlCollectionSummaryDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlGroupFileSummaryDto;

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

@Path("xml-collection")
@Tag(name = "XmlCollection", description = """
        XML collector configuration API V2 (the contents of the former `xml-datacollection-config.xml` and
        `xml-datacollection/*.xml`).

        A collection is the unit of transfer: `GET`, `POST` and `PUT` carry the whole collection, including its
        sources and the groups defined in them. Sources and groups have no identity of their own, so a source left
        out of a `PUT` body is deleted. A collection is used by a collectd service through its `collection`
        parameter.

        Group files hold groups that several sources share; a source refers to them by id in `importGroups`.
        Entries stored as the former file path (`xml-datacollection/foo.xml`) are reported as the id (`foo`).

        `GET` on a collection or group file returns an `ETag`. Sending it back as `If-Match` on a write makes the
        write fail with a 412 rather than overwrite a change someone else made in the meantime. Omitting
        `If-Match` keeps the last writer.

        Every endpoint, reads included, requires ROLE_ADMIN: source URLs, request parameters and headers can carry
        credentials.

        Changes take effect with the next collection; neither collectd nor OpenNMS needs a restart.""")
public interface XmlCollectionConfigRestApi {

    String COLLECTION_JSON_EXAMPLE = """
            {
              "name": "xml-elasticsearch-cluster-stats",
              "rrd": {
                "step": 300,
                "rras": [ "RRA:AVERAGE:0.5:1:2016", "RRA:AVERAGE:0.5:12:1488", "RRA:MAX:0.5:288:366" ]
              },
              "sources": [
                {
                  "url": "http://{ipaddr}:9200/_cluster/stats",
                  "request": null,
                  "importGroups": [ "elasticsearch-cluster-stats" ],
                  "groups": []
                }
              ]
            }""";

    String GROUP_FILE_JSON_EXAMPLE = """
            {
              "id": "elasticsearch-cluster-stats",
              "groups": [
                {
                  "name": "cluster-stats-nodes",
                  "resourceType": "node",
                  "resourceXpath": "/",
                  "resourceKeys": [],
                  "objects": [
                    { "name": "nodesCountTotal", "type": "gauge", "xpath": "nodes/count/total", "mappings": [] },
                    { "name": "status", "type": "gauge", "xpath": "status",
                      "mappings": [ { "from": "green", "to": "0" }, { "from": "yellow", "to": "1" }, { "from": "red", "to": "2" } ] }
                  ]
                }
              ]
            }""";

    // ---------------------------------------------------------------- settings

    @GET
    @Path("settings")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the XML collector settings",
            description = "Settings shared by every collection, currently the RRD repository.",
            operationId = "getXmlCollectionSettings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settings retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = XmlCollectionSettingsDto.class))),
            @ApiResponse(responseCode = "500", description = "Failed to read the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getSettings(@Context SecurityContext securityContext);

    @PUT
    @Path("settings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the XML collector settings", description = "Requires ROLE_ADMIN.",
            operationId = "updateXmlCollectionSettings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Settings updated. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or RRD repository.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Failed to persist the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateSettings(XmlCollectionSettingsDto settings, @Context SecurityContext securityContext);

    // ------------------------------------------------------------- collections

    @GET
    @Path("collections")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List XML data collections",
            description = "List every collection without its sources, sorted by name.",
            operationId = "getXmlCollections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collections retrieved successfully. An empty array when no configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = XmlCollectionSummaryDto.class)))),
            @ApiResponse(responseCode = "500", description = "Failed to read the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getCollections(@Context SecurityContext securityContext);

    @GET
    @Path("collections/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get an XML data collection",
            description = "Retrieve one collection with its sources and groups. The response carries an `ETag` to be used as `If-Match` on a later write.",
            operationId = "getXmlCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = XmlCollectionDto.class),
                            examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getCollection(
            @Parameter(description = "Name of the collection.", example = "xml-elasticsearch-cluster-stats")
            @PathParam("name") String name,
            @Context SecurityContext securityContext);

    @POST
    @Path("collections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create an XML data collection",
            description = "Create a new collection. Requires ROLE_ADMIN. The name must not already be in use and every imported group file must exist.",
            operationId = "createXmlCollection")
    @RequestBody(required = true, description = "The collection to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = XmlCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collection created. The `Location` header points at the new collection."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Collection 'foo', source 1: the URL is required."))),
            @ApiResponse(responseCode = "409", description = "A collection of that name already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response createCollection(XmlCollectionDto collection, @Context SecurityContext securityContext);

    @PUT
    @Path("collections/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace an XML data collection",
            description = """
        Replace a collection in full. Requires ROLE_ADMIN. The body is not a patch: sources and groups missing
        from it are removed. Giving the body a different `name` renames the collection, provided the new name is
        free. Collectd services referring to the old name through their `collection` parameter are not changed.""",
            operationId = "updateXmlCollection")
    @RequestBody(required = true, description = "The complete collection.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = XmlCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new name is already taken by another collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored collection; it changed since it was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateCollection(
            @Parameter(description = "Name of the collection to replace.", example = "xml-elasticsearch-cluster-stats")
            @PathParam("name") String name,
            XmlCollectionDto collection,
            @Parameter(description = "Version of the collection as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("collections/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete an XML data collection", description = "Requires ROLE_ADMIN.",
            operationId = "deleteXmlCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No collection of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteCollection(
            @Parameter(description = "Name of the collection to delete.", example = "xml-elasticsearch-cluster-stats")
            @PathParam("name") String name,
            @Parameter(description = "Version of the collection as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    // ------------------------------------------------------------- group files

    @GET
    @Path("groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List group files",
            description = "List every group file without its groups, sorted by id, with the collections importing it.",
            operationId = "getXmlGroupFiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group files retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = XmlGroupFileSummaryDto.class)))),
            @ApiResponse(responseCode = "500", description = "Failed to read the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getGroupFiles(@Context SecurityContext securityContext);

    @GET
    @Path("groups/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a group file",
            description = "Retrieve one group file with its groups. The response carries an `ETag` to be used as `If-Match` on a later write.",
            operationId = "getXmlGroupFile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group file retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = XmlGroupFileDto.class),
                            examples = @ExampleObject(value = GROUP_FILE_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No group file with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getGroupFile(
            @Parameter(description = "Id of the group file.", example = "elasticsearch-cluster-stats")
            @PathParam("id") String id,
            @Context SecurityContext securityContext);

    @POST
    @Path("groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a group file", description = "Requires ROLE_ADMIN. The id must not already be in use.",
            operationId = "createXmlGroupFile")
    @RequestBody(required = true, description = "The group file to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = XmlGroupFileDto.class),
                    examples = @ExampleObject(value = GROUP_FILE_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group file created. The `Location` header points at it."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A group file with that id already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response createGroupFile(XmlGroupFileDto groupFile, @Context SecurityContext securityContext);

    @PUT
    @Path("groups/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace a group file",
            description = "Replace the groups of a group file in full. Requires ROLE_ADMIN. The id cannot be changed; the body's `id` must be absent or equal to the path.",
            operationId = "updateXmlGroupFile")
    @RequestBody(required = true, description = "The complete group file.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = XmlGroupFileDto.class),
                    examples = @ExampleObject(value = GROUP_FILE_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group file replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body, a different id, or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No group file with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored group file.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateGroupFile(
            @Parameter(description = "Id of the group file to replace.", example = "elasticsearch-cluster-stats")
            @PathParam("id") String id,
            XmlGroupFileDto groupFile,
            @Parameter(description = "Version of the group file as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("groups/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a group file",
            description = "Requires ROLE_ADMIN. A group file that a source still imports cannot be deleted; remove it from those sources first.",
            operationId = "deleteXmlGroupFile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group file deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No group file with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A collection still imports the group file.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group file 'elasticsearch-cluster-stats' is imported by collections: xml-elasticsearch-cluster-stats."))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored group file.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteGroupFile(
            @Parameter(description = "Id of the group file to delete.", example = "elasticsearch-cluster-stats")
            @PathParam("id") String id,
            @Parameter(description = "Version of the group file as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    // ---------------------------------------------------------------- metadata

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get editor metadata",
            description = "Attribute types, resource types and group file ids, for the choices the editor offers.",
            operationId = "getXmlCollectionMetadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = XmlCollectionMetadataDto.class)))
    })
    Response getMetadata(@Context SecurityContext securityContext);

    // ------------------------------------------------------- download / upload

    @GET
    @Path("download")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Download the XML collector configuration",
            description = "Download the settings, every collection and every group file as one JSON document (`xml-collection.json`). Requires ROLE_ADMIN. The document is accepted back by `POST /xml-collection/upload`.",
            operationId = "downloadXmlCollectionConfiguration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = XmlCollectionConfigDto.class))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadConfiguration(@Context SecurityContext securityContext);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload the XML collector configuration in JSON format",
            description = """
        Replace the whole configuration: settings, collections and group files. Requires ROLE_ADMIN. The single
        part must be named `upload` and hold a document of the shape `GET /xml-collection/download` produces.
        Group files missing from the document are deleted.""",
            operationId = "uploadXmlCollectionConfiguration")
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the JSON configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadConfiguration(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload a legacy XML file",
            description = """
        Import one file in the former on-disk format. Requires ROLE_ADMIN. The part must be named `upload`.

        * An `<xml-datacollection-config>` document (the former `xml-datacollection-config.xml`) replaces the
          settings and every collection. Every group file it imports must exist, so upload the group files first.
        * An `<xml-groups>` document (a former `xml-datacollection/*.xml`) creates or replaces the group file whose
          id is given in the optional `id` part, or else is the uploaded file name without `.xml`.""",
            operationId = "uploadXmlCollectionXml")
    @RequestBody(required = true,
            description = "Multipart form with an `upload` part holding the XML file and an optional `id` part.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File imported. The body tells what was imported: `{\"type\":\"groupFile\",\"id\":\"foo\"}` or `{\"type\":\"collections\",\"count\":2}`."),
            @ApiResponse(responseCode = "400", description = "Unparseable XML, an unknown root element, a missing group file or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadXml(@Multipart("upload") Attachment attachment,
                       @Multipart(value = "id", required = false) String id,
                       @Context SecurityContext securityContext);
}
