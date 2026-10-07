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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.web.rest.v2.model.GraphCollectionDto;
import org.opennms.web.rest.v2.model.GraphCollectionGraphDto;
import org.opennms.web.rest.v2.model.GraphCollectionResolvedDto;
import org.opennms.web.rest.v2.model.GraphCollectionTimespanDto;

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

@Path("graph-collections")
@Tag(name = "GraphCollections", description = """
        Graph Collections API, backed by `ksc-performance-reports.xml`. Graph collections were formerly
        called KSC reports and this API replaces `/rest/ksc`.

        A graph collection is a titled, ordered list of graphs plus the layout options of its view page. Each
        graph carries a `kind`: `prefab`, a prefabricated graph drawn for one resource over a named timespan,
        is the only kind stored today; `adhoc`, reserved for graphs from the ad-hoc graph builder, is rejected
        with 400 until it is implemented. A graph without a `kind` is `prefab`. Ids are assigned by the server.
        The whole collection is the unit of update: `PUT /graph-collections/{id}` replaces title, options
        and graph list in one call, which is how graphs are edited, reordered and removed.

        Any authenticated user may read and write collections, except users holding `ROLE_READONLY`, who
        may only read. Validation failures return a `text/plain` message with status 400.

        The `resolved` operations return a collection prepared for rendering: view-time timespan and
        graph type overrides applied, named timespans turned into start and end times, and every graph's
        resource and prefabricated graph looked up, with graphs whose resource has gone marked invalid
        rather than silently dropped.""")
public interface GraphCollectionRestApi {

    String COLLECTION_EXAMPLE = """
            {
              "id": 3,
              "title": "Core routers",
              "showTimespanButton": true,
              "showGraphtypeButton": false,
              "graphsPerLine": 2,
              "graphs": [
                {
                  "kind": "prefab",
                  "title": "Uplink traffic",
                  "resourceId": "node[Routers:rtr-01].interfaceSnmp[ge-0_0_0-00112233aabb]",
                  "graphtype": "mib2.HCbits",
                  "timespan": "7_day",
                  "extlink": null,
                  "nodeId": null,
                  "nodeSource": null,
                  "domain": null,
                  "interfaceId": null
                }
              ]
            }""";

    String RESOLVED_EXAMPLE = """
            {
              "id": 3,
              "type": "custom",
              "title": "Core routers",
              "showTimespanButton": true,
              "showGraphtypeButton": false,
              "graphsPerLine": 2,
              "timespan": "1_day",
              "graphtype": null,
              "graphTypes": [ "mib2.HCbits", "mib2.HCerrors", "mib2.HCpackets" ],
              "graphs": [
                {
                  "index": 0,
                  "kind": "prefab",
                  "title": "Uplink traffic",
                  "timespan": "1_day",
                  "graphtype": "mib2.HCbits",
                  "resourceId": "node[Routers:rtr-01].interfaceSnmp[ge-0_0_0-00112233aabb]",
                  "resource": {
                    "id": "node[Routers:rtr-01].interfaceSnmp[ge-0_0_0-00112233aabb]",
                    "label": "ge-0/0/0 (10.0.0.1)",
                    "typeName": "interfaceSnmp",
                    "typeLabel": "SNMP Interface Data",
                    "link": "element/snmpinterface.jsp?node=12&ifindex=1",
                    "parent": {
                      "id": "node[Routers:rtr-01]",
                      "label": "rtr-01",
                      "typeName": "node",
                      "typeLabel": "Node",
                      "link": "element/node.jsp?node=12",
                      "parent": null
                    }
                  },
                  "prefabGraphTitle": "Bits In/Out (High Speed)",
                  "valid": true,
                  "error": null,
                  "start": 1759600000000,
                  "end": 1759686400000,
                  "extlink": null
                }
              ]
            }""";

    String TIMESPAN_VALUES = "One of the named timespans listed by `GET /graph-collections/timespans`, for example `1_hour`, `7_day`, `This Month` or `Last Quarter`.";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List graph collections",
            description = "Return every graph collection, with its graphs, sorted by title.",
            operationId = "listGraphCollections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The graph collections.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = GraphCollectionDto.class)),
                            examples = @ExampleObject(value = "[" + COLLECTION_EXAMPLE + "]")))
    })
    Response listCollections();

    @GET
    @Path("timespans")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List the named timespans",
            description = "Return the named timespans a graph may use, in display order, with the label shown for each.",
            operationId = "listGraphCollectionTimespans")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The timespans.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = GraphCollectionTimespanDto.class)),
                            examples = @ExampleObject(value = """
                            [ { "id": "1_hour", "label": "1 hour" }, { "id": "7_day", "label": "7 day" }, { "id": "This Month", "label": "This Month" } ]""")))
    })
    Response listTimespans();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one graph collection", operationId = "getGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The graph collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionDto.class),
                            examples = @ExampleObject(value = COLLECTION_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No collection with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Graph collection 3 was not found.")))
    })
    Response getCollection(
            @Parameter(description = "Collection id.", required = true, example = "3")
            @PathParam("id") int id);

    @GET
    @Path("{id}/resolved")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a graph collection prepared for rendering",
            description = """
            Return the collection with the view-time overrides applied and every graph's resource, prefabricated
            graph and start and end times resolved. Graphs whose resource no longer exists, or whose
            prefabricated graph is not available for that resource, come back with `valid` false and an
            `error`; nothing is removed from the stored collection.""",
            operationId = "getResolvedGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The resolved collection.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionResolvedDto.class),
                            examples = @ExampleObject(value = RESOLVED_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "The `timespan` override is not a known timespan.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response getResolvedCollection(
            @Parameter(description = "Collection id.", required = true, example = "3")
            @PathParam("id") int id,
            @Parameter(description = "Timespan to apply to every graph instead of its own. " + TIMESPAN_VALUES + " Omit, or pass `none`, for no override.", example = "1_day")
            @QueryParam("timespan") String timespan,
            @Parameter(description = "Prefabricated graph name to draw for every graph instead of its own. Omit, or pass `none`, for no override.", example = "mib2.HCbits")
            @QueryParam("graphtype") String graphtype);

    @GET
    @Path("resolved/node/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Build and resolve a collection of a node's SNMP interface graphs",
            description = """
            Build a collection on the fly with one graph per SNMP interface of the node, using the first
            prefabricated graph available for each interface and a 7 day timespan, and return it resolved as
            `GET /graph-collections/{id}/resolved` would. Nothing is stored; to keep it, post the graphs as a new
            collection.""",
            operationId = "getResolvedNodeGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The resolved collection, with `type` `node` and no id.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionResolvedDto.class))),
            @ApiResponse(responseCode = "400", description = "The `timespan` override is not a known timespan.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "The node does not exist or has no resources.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response getResolvedNodeCollection(
            @Parameter(description = "Database id of the node.", required = true, example = "12")
            @PathParam("nodeId") int nodeId,
            @Parameter(description = "Timespan override, as for `GET /graph-collections/{id}/resolved`.", example = "1_day")
            @QueryParam("timespan") String timespan,
            @Parameter(description = "Graph type override, as for `GET /graph-collections/{id}/resolved`.", example = "mib2.HCbits")
            @QueryParam("graphtype") String graphtype);

    @GET
    @Path("resolved/nodeSource/{nodeSource}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Build and resolve a collection of a node's SNMP interface graphs, by foreign source and id",
            description = "As `GET /graph-collections/resolved/node/{nodeId}`, addressing the node as `foreignSource:foreignId`.",
            operationId = "getResolvedNodeSourceGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The resolved collection, with `type` `nodeSource` and no id.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionResolvedDto.class))),
            @ApiResponse(responseCode = "400", description = "The `timespan` override is not a known timespan.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "The node does not exist or has no resources.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response getResolvedNodeSourceCollection(
            @Parameter(description = "The node's `foreignSource:foreignId`.", required = true, example = "Routers:rtr-01")
            @PathParam("nodeSource") String nodeSource,
            @Parameter(description = "Timespan override, as for `GET /graph-collections/{id}/resolved`.", example = "1_day")
            @QueryParam("timespan") String timespan,
            @Parameter(description = "Graph type override, as for `GET /graph-collections/{id}/resolved`.", example = "mib2.HCbits")
            @QueryParam("graphtype") String graphtype);

    @GET
    @Path("resolved/domain/{domain}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Build and resolve a collection of a domain's SNMP interface graphs",
            description = "As `GET /graph-collections/resolved/node/{nodeId}`, for the interfaces collected under a domain.",
            operationId = "getResolvedDomainGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The resolved collection, with `type` `domain` and no id.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionResolvedDto.class))),
            @ApiResponse(responseCode = "400", description = "The `timespan` override is not a known timespan.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "The domain has no resources.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response getResolvedDomainCollection(
            @Parameter(description = "Domain name.", required = true, example = "example.com")
            @PathParam("domain") String domain,
            @Parameter(description = "Timespan override, as for `GET /graph-collections/{id}/resolved`.", example = "1_day")
            @QueryParam("timespan") String timespan,
            @Parameter(description = "Graph type override, as for `GET /graph-collections/{id}/resolved`.", example = "mib2.HCbits")
            @QueryParam("graphtype") String graphtype);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a graph collection",
            description = """
            Create a collection and write it to `ksc-performance-reports.xml`. The server assigns the id; an id in
            the body is ignored. Graphs may be included inline. To copy an existing collection, get it and post
            it back with a new title.""",
            operationId = "createGraphCollection")
    @RequestBody(required = true, description = "The collection to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at the new collection, which is also returned in the body.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionDto.class))),
            @ApiResponse(responseCode = "400", description = "The body is missing or invalid: no title, a negative `graphsPerLine`, or a graph without a resource, graph type or known timespan.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Graph 1: unknown timespan 'fortnight'."))),
            @ApiResponse(responseCode = "403", description = "The user holds `ROLE_READONLY`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The configuration file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response createCollection(@Context SecurityContext securityContext, @Context UriInfo uriInfo, GraphCollectionDto collection);

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace a graph collection",
            description = """
            Replace the collection's title, options and graph list with the body. This is the one write for
            editing: send the full graph list in the order wanted to edit, reorder or remove graphs. The id in
            the path wins over any id in the body.""",
            operationId = "updateGraphCollection")
    @RequestBody(required = true, description = "The full replacement collection.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphCollectionDto.class),
                    examples = @ExampleObject(value = COLLECTION_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The collection as saved.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionDto.class))),
            @ApiResponse(responseCode = "400", description = "The body is missing or invalid.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "The user holds `ROLE_READONLY`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The configuration file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response updateCollection(@Context SecurityContext securityContext,
            @Parameter(description = "Collection id.", required = true, example = "3")
            @PathParam("id") int id,
            GraphCollectionDto collection);

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a graph collection", operationId = "deleteGraphCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted."),
            @ApiResponse(responseCode = "403", description = "The user holds `ROLE_READONLY`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The configuration file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response deleteCollection(@Context SecurityContext securityContext,
            @Parameter(description = "Collection id.", required = true, example = "3")
            @PathParam("id") int id);

    @POST
    @Path("{id}/graphs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a graph to a graph collection",
            description = """
            Append one graph to the collection, or insert it at `index` when given. This is the "add to
            collection" action used from graph pages; editing an existing graph is done by replacing the
            collection with `PUT /graph-collections/{id}`.""",
            operationId = "addGraphToGraphCollection")
    @RequestBody(required = true, description = "The graph to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphCollectionGraphDto.class),
                    examples = @ExampleObject(value = """
                    { "kind": "prefab", "title": "Uplink traffic", "resourceId": "node[Routers:rtr-01].interfaceSnmp[ge-0_0_0-00112233aabb]", "graphtype": "mib2.HCbits", "timespan": "7_day" }""")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The collection as saved, with the new graph in place.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GraphCollectionDto.class))),
            @ApiResponse(responseCode = "400", description = "The graph is missing or invalid, or `index` is out of range.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "The user holds `ROLE_READONLY`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No collection with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The configuration file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response addGraph(@Context SecurityContext securityContext,
            @Parameter(description = "Collection id.", required = true, example = "3")
            @PathParam("id") int id,
            @Parameter(description = "Zero-based position to insert at. Omit to append.", example = "0")
            @QueryParam("index") Integer index,
            GraphCollectionGraphDto graph);

    @POST
    @Path("reload")
    @Operation(summary = "Reload the graph collections configuration",
            description = "Re-read `ksc-performance-reports.xml` from disk, picking up edits made outside this API.",
            operationId = "reloadGraphCollections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reloaded."),
            @ApiResponse(responseCode = "403", description = "The user holds `ROLE_READONLY`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The file could not be read or parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    Response reload(@Context SecurityContext securityContext);
}
