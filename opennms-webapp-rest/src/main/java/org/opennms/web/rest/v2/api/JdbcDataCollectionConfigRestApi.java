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
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcDataCollectionMetadataDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;
import org.opennms.web.rest.v2.model.JdbcSourceSummaryDto;

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

@Path("jdbc-datacollection")
@Tag(name = "JdbcDataCollection", description = """
        JDBC data collection configuration API V2 (the contents of the former `jdbc-datacollection-config.xml`
        and `jdbc-datacollection.d/*.xml`).

        A source is one stored configuration: `default` holds the former `jdbc-datacollection-config.xml`, and
        every former file of `jdbc-datacollection.d/` became a source named after the file. A source holds
        collections, a collection holds queries, a query holds the columns to store. The JdbcCollector sees
        all sources merged: `default` first, then the others sorted by id. A collectd service selects a
        collection by its `collection` parameter, so a collection name must be unique across all sources.

        The collection is the unit of transfer below a source: `POST` and `PUT` carry the whole collection,
        including its queries and columns. A query left out of a `PUT` body is deleted.

        `GET` on a source or a collection returns the `ETag` of the source. Sending it back as `If-Match` on
        a write makes the write fail with a 412 rather than overwrite a change someone else made in the
        meantime. Omitting `If-Match` keeps the last writer.

        Changes take effect with the next collection; no daemon reload is needed.""")
public interface JdbcDataCollectionConfigRestApi {

    String COLLECTION_JSON_EXAMPLE = """
            {
              "name": "PostgreSQL",
              "rrdStep": 300,
              "rras": ["RRA:AVERAGE:0.5:1:2016", "RRA:MAX:0.5:288:366"],
              "queries": [
                {
                  "name": "pg_tablespace_size",
                  "recheckInterval": 0,
                  "ifType": "all",
                  "resourceType": "pgTableSpace",
                  "instanceColumn": "spcname",
                  "queryString": "SELECT spcname, pg_tablespace_size(spcname) AS ts_size FROM pg_tablespace",
                  "columns": [
                    { "name": "spcname", "type": "string", "alias": "spcname" },
                    { "name": "ts_size", "type": "gauge", "alias": "ts_size" }
                  ]
                }
              ]
            }""";

    String SOURCE_JSON_EXAMPLE = """
            {
              "id": "postgresql",
              "rrdRepository": "${install.share.dir}/rrd/snmp/",
              "collections": []
            }""";

    // ------------------------------------------------------------------ sources

    @GET
    @Path("sources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List sources",
            description = "List every source without its collections, in the order the collector merges them: `default` first, then sorted by id.",
            operationId = "getJdbcSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = JdbcSourceSummaryDto.class)))),
            @ApiResponse(responseCode = "500", description = "Failed to read the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getSources(@Context SecurityContext securityContext);

    @GET
    @Path("sources/{sourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a source",
            description = """
        Retrieve one source with all of its collections. Each collection lists the collectd services that use
        it, and is marked when an earlier source defines a collection of the same name. The response carries
        an `ETag` to be used as `If-Match` on a later write to the source or one of its collections.""",
            operationId = "getJdbcSource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcSourceDto.class))),
            @ApiResponse(responseCode = "404", description = "No source with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "JDBC data collection source 'mysql' does not exist.")))
    })
    Response getSource(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                       @Context SecurityContext securityContext);

    @POST
    @Path("sources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a source",
            description = "Create a source. Requires ROLE_ADMIN. The body may contain collections; their names must not be used by another source.",
            operationId = "createJdbcSource")
    @RequestBody(required = true, description = "The source to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcSourceDto.class),
                    examples = @ExampleObject(value = SOURCE_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Source created. The `Location` header points at the new source."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The id or a collection name is already in use.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "JDBC data collection source 'postgresql' already exists.")))
    })
    Response createSource(JdbcSourceDto source, @Context SecurityContext securityContext);

    @PUT
    @Path("sources/{sourceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the settings of a source",
            description = """
        Update the RRD repository of a source. Requires ROLE_ADMIN. The collections and the id in the body are
        ignored: collections are written through their own resources, and a source cannot be renamed.""",
            operationId = "updateJdbcSource")
    @RequestBody(required = true, description = "The source; only `rrdRepository` is used.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcSourceDto.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Source updated. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No source with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "The source changed since the `If-Match` version was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateSource(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                          JdbcSourceDto source,
                          @Parameter(description = "ETag of the source as returned by a previous GET.") @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                          @Context SecurityContext securityContext);

    @DELETE
    @Path("sources/{sourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a source",
            description = """
        Delete a source with all of its collections. Requires ROLE_ADMIN. The `default` source cannot be
        deleted. Collectd services that use one of its collections stop collecting.""",
            operationId = "deleteJdbcSource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Source deleted. No response body."),
            @ApiResponse(responseCode = "400", description = "The `default` source cannot be deleted.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No source with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "The source changed since the `If-Match` version was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteSource(@Parameter(description = "Id of the source.", example = "mysql") @PathParam("sourceId") String sourceId,
                          @Parameter(description = "ETag of the source as returned by a previous GET.") @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                          @Context SecurityContext securityContext);

    // -------------------------------------------------------------- collections

    @GET
    @Path("sources/{sourceId}/collections/{collectionName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a collection",
            description = "Retrieve one collection with all of its queries. The `ETag` is the one of the source.",
            operationId = "getJdbcCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcCollectionDto.class),
                            examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No such source or collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response getCollection(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                           @Parameter(description = "Name of the collection.", example = "PostgreSQL") @PathParam("collectionName") String collectionName,
                           @Context SecurityContext securityContext);

    @POST
    @Path("sources/{sourceId}/collections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a collection",
            description = "Add a collection to a source. Requires ROLE_ADMIN. The name must not be used by any source.",
            operationId = "createJdbcCollection")
    @RequestBody(required = true, description = "The collection to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collection created. The `Location` header points at the new collection."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Collection 'PostgreSQL': the RRD step must be a positive number of seconds."))),
            @ApiResponse(responseCode = "404", description = "No source with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A collection of that name already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Collection 'PostgreSQL' already exists in source 'postgresql'."))),
            @ApiResponse(responseCode = "412", description = "The source changed since the `If-Match` version was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response createCollection(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                              JdbcCollectionDto collection,
                              @Parameter(description = "ETag of the source as returned by a previous GET.") @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                              @Context SecurityContext securityContext);

    @PUT
    @Path("sources/{sourceId}/collections/{collectionName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace a collection",
            description = """
        Replace a collection in full. Requires ROLE_ADMIN. The body is not a patch: queries missing from it are
        removed. Giving the body a different `name` renames the collection, provided no source uses the new
        name. Collectd services are not changed by a rename and keep using the old name.""",
            operationId = "updateJdbcCollection")
    @RequestBody(required = true, description = "The complete collection.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No such source or collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new name is already in use.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "The source changed since the `If-Match` version was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response updateCollection(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                              @Parameter(description = "Name of the collection.", example = "PostgreSQL") @PathParam("collectionName") String collectionName,
                              JdbcCollectionDto collection,
                              @Parameter(description = "ETag of the source as returned by a previous GET.") @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                              @Context SecurityContext securityContext);

    @DELETE
    @Path("sources/{sourceId}/collections/{collectionName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a collection",
            description = "Delete a collection. Requires ROLE_ADMIN. Collectd services that use it stop collecting.",
            operationId = "deleteJdbcCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collection deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No such source or collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "The source changed since the `If-Match` version was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteCollection(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                              @Parameter(description = "Name of the collection.", example = "PostgreSQL") @PathParam("collectionName") String collectionName,
                              @Parameter(description = "ETag of the source as returned by a previous GET.") @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                              @Context SecurityContext securityContext);

    // ------------------------------------------------------------------ helpers

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get editor metadata",
            description = "The selectable column types, suggested interface types, the resource types of this system and the defaults of a new source or collection.",
            operationId = "getJdbcDataCollectionMetadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcDataCollectionMetadataDto.class)))
    })
    Response getMetadata(@Context SecurityContext securityContext);

    @GET
    @Path("sources/{sourceId}/download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Download a source",
            description = "Download a source as a file. Requires ROLE_ADMIN. The XML has the format of the former files and can be uploaded again.",
            operationId = "downloadJdbcSource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The source as an attachment."),
            @ApiResponse(responseCode = "400", description = "Unsupported format.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No source with that id exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadSource(@Parameter(description = "Id of the source.", example = "postgresql") @PathParam("sourceId") String sourceId,
                            @Parameter(description = "`xml` (default) or `json`.", example = "xml") @QueryParam("format") String format,
                            @Context SecurityContext securityContext);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload a source",
            description = """
        Create or replace a source from a file. Requires ROLE_ADMIN. The file is either XML in the format of the
        former `jdbc-datacollection.d/*.xml` files (validated against the schema) or JSON as returned by the
        JSON download. The source id is taken from the `id` parameter, else from the JSON body, else from the
        file name without its extension. An existing source is only replaced with `replace=true`. Collection
        names must not be used by another source.""",
            operationId = "uploadJdbcSource")
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the XML or JSON file.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source stored. The body is the summary of the stored source.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcSourceSummaryDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing file, unreadable content or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The source exists and `replace` is not set, or a collection name is used by another source.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadSource(@Multipart("upload") Attachment attachment,
                          @Parameter(description = "Id of the source to create or replace.", example = "mysql") @QueryParam("id") String sourceId,
                          @Parameter(description = "Replace an existing source of that id.") @QueryParam("replace") boolean replace,
                          @Context SecurityContext securityContext);
}
