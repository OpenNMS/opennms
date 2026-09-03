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
package org.opennms.web.rest.v1;

import static org.opennms.web.svclayer.support.DefaultGraphResultsService.RESOURCE_IDS_CONTEXT;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
import org.apache.commons.lang3.StringUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.netmgt.model.resource.ResourceDTOCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * Read-only API for retrieving resources.
 *
 * @author jwhite
 */
@Component("resourceRestService")
@Path("resources")
@Tag(name = "Resources", description = """
        The tree of entities that performance data is collected against.

        ## Resource ids

        A resource id is an opaque string with a fixed grammar: a dot-separated path of segments, each
        segment being a resource type followed by that type's instance name in square brackets:

            <type>[<instance>]{.<type>[<instance>]}

        The root segment is always `node[...]`, and the node instance is either the database id
        (`node[1]`) or `foreignSource:foreignId` (`node[loopback-lab:lb-001]`). Which of the two forms
        appears is decided by the node, not by how it was looked up: a node that came from a requisition is
        always reported in the `foreignSource:foreignId` form. Child segments name the sub-resource type and
        its instance, for example `.responseTime[127.0.0.1]`, `.interfaceSnmp[eth0-005056b6b6b6]`,
        `.perspectiveResponseTime[127.0.0.1@Default]`.

        Ids therefore contain `[`, `]`, `:` and `@`, all of which have to be percent-encoded when the id is
        used as a path segment: `node%5Bloopback-lab%3Alb-001%5D.responseTime%5B127.0.0.1%5D`.

        `GET /resources/fornode/{nodeCriteria}` takes a database id or `foreignSource:foreignId` and returns
        the node's resource with its children. The ids that can be graphed are the children carrying
        `rrdGraphAttributes`; the attribute keys in that map are the attribute names the Measurements API
        takes.

        A resource id that does not parse is reported as 500, not 400. An id that parses but names nothing is
        a 404.""")
public class ResourceRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRestService.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private FilterDao m_filterDao;

    @Autowired
    private JsonStore m_jsonStore;

    private final Gson m_gson = new Gson();

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "List top-level resources",
            description = """
        List the top-level resources, which in practice means one entry per node that has collected data,
        plus any non-node top-level resources the running resource types define.

        `depth` controls how far the children are walked. The default of 1 returns each top-level resource
        with its immediate children. A negative depth walks the whole subtree.

        This operation is not paged: `totalCount` and `count` are always equal and `offset` is always 0.""",
            operationId = "getResources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The top-level resources.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ResourceDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 254,
                      "count": 254,
                      "offset": 0,
                      "resource": [
                        {
                          "id": "node[loopback-lab:lb-001]",
                          "label": "loopback-001",
                          "name": "loopback-lab:lb-001",
                          "link": "element/node.jsp?node=loopback-lab:lb-001",
                          "typeLabel": "Node",
                          "parentId": null,
                          "stringPropertyAttributes": {},
                          "externalValueAttributes": {},
                          "rrdGraphAttributes": {}
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ResourceDTOCollection.class))
                    })
    })
    public ResourceDTOCollection getResources(
            @Parameter(description = "How many levels of children to include. 1 returns the immediate children; a negative value walks the whole subtree.",
                    example = "1")
            @DefaultValue("1") @QueryParam("depth") final int depth) {
        List<ResourceDTO> resources = Lists.newLinkedList();
        for (OnmsResource resource : m_resourceDao.findTopLevelResources()) {
            resources.add(ResourceDTO.fromResource(resource, depth));
        }
        return new ResourceDTOCollection(resources);
    }

    @GET
    @Path("{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "Get one resource by id",
            description = """
        Return a single resource and, by default, its whole subtree. The path segment is a resource id and
        has to be percent-encoded, since ids contain `[`, `]` and often `:`.

        `rrdGraphAttributes` names the numeric attributes on the resource; each entry gives the attribute
        name and where its RRD/JRB file sits under the share directory. `stringPropertyAttributes` holds
        collected strings, `externalValueAttributes` holds values sourced from outside the collector.

        An id whose grammar does not parse fails with 500 rather than 400.""",
            operationId = "getResourceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The resource.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ResourceDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "node[loopback-lab:lb-001]",
                      "label": "loopback-001",
                      "name": "loopback-lab:lb-001",
                      "link": "element/node.jsp?node=loopback-lab:lb-001",
                      "typeLabel": "Node",
                      "parentId": null,
                      "children": {
                        "totalCount": 1,
                        "count": 1,
                        "offset": 0,
                        "resource": [
                          {
                            "id": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                            "label": "Response Time for 127.0.0.1",
                            "name": "127.0.0.1",
                            "link": "element/interface.jsp?node=loopback-lab:lb-001&intf=127.0.0.1",
                            "typeLabel": "Response Time",
                            "parentId": "node[loopback-lab:lb-001]",
                            "stringPropertyAttributes": {},
                            "externalValueAttributes": {},
                            "rrdGraphAttributes": {
                              "http-8080": {
                                "name": "http-8080",
                                "relativePath": "response/127.0.0.1",
                                "rrdFile": "http-8080.rrd"
                              }
                            }
                          }
                        ]
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ResourceDTO.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The id parses but names no resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No resource with id 'node[99999]' found."))),
            @ApiResponse(responseCode = "500", description = "The id does not match the resource-id grammar.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Ill-formed resource ID: not-an-id")))
    })
    public ResourceDTO getResourceById(
            @Parameter(description = "Percent-encoded resource id.", required = true,
                    example = "node[loopback-lab:lb-001].responseTime[127.0.0.1]")
            @PathParam("resourceId") final String resourceId,
            @Parameter(description = "How many levels of children to include. The default of -1 walks the whole subtree.",
                    example = "-1")
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.fromString(resourceId));
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }

        return ResourceDTO.fromResource(resource, depth);
    }

    @DELETE
    @Path("{resourceId}")
    @Transactional(readOnly=false)
    @Operation(
            summary = "Delete a resource and its collected data",
            description = """
        Remove the resource and the persisted data behind it, including files under the share directory.

        The response carries no body on success. An id whose grammar does not parse fails with 500 rather
        than 400.""",
            operationId = "deleteResourceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The resource was deleted."),
            @ApiResponse(responseCode = "404", description = "The id parses but names no resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No resource with id 'node[99999]' found."))),
            @ApiResponse(responseCode = "500", description = "The id does not match the resource-id grammar.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Ill-formed resource ID: not-an-id")))
    })
    public void deleteResourceById(
            @Parameter(description = "Percent-encoded resource id.", required = true,
                    example = "node[loopback-lab:lb-001].responseTime[127.0.0.1]")
            @PathParam("resourceId") final String resourceId) {
        final boolean found = m_resourceDao.deleteResourceById(ResourceId.fromString(resourceId));

        if (!found) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }
    }


    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "Get the resource tree for a node",
            description = """
        Return the node's own resource with, by default, its whole subtree.

        `nodeCriteria` is either the database node id (`1`) or `foreignSource:foreignId`
        (`loopback-lab:lb-001`). A criteria string that is neither numeric nor contains a colon fails with
        500 while trying to parse it as a number, rather than with 404.""",
            operationId = "getResourceForNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node resource.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ResourceDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "node[loopback-lab:lb-001]",
                      "label": "loopback-001",
                      "name": "loopback-lab:lb-001",
                      "link": "element/node.jsp?node=loopback-lab:lb-001",
                      "typeLabel": "Node",
                      "parentId": null,
                      "children": {
                        "totalCount": 1,
                        "count": 1,
                        "offset": 0,
                        "resource": [
                          {
                            "id": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                            "label": "Response Time for 127.0.0.1",
                            "name": "127.0.0.1",
                            "link": "element/interface.jsp?node=loopback-lab:lb-001&intf=127.0.0.1",
                            "typeLabel": "Response Time",
                            "parentId": "node[loopback-lab:lb-001]",
                            "stringPropertyAttributes": {},
                            "externalValueAttributes": {},
                            "rrdGraphAttributes": {
                              "http-8080": {
                                "name": "http-8080",
                                "relativePath": "response/127.0.0.1",
                                "rrdFile": "http-8080.rrd"
                              }
                            }
                          }
                        ]
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ResourceDTO.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No such node, or the node has no resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No node found with criteria 'loopback-lab:nope'."))),
            @ApiResponse(responseCode = "500", description = "The criteria is neither a node id nor a foreignSource:foreignId pair.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"not-a-node\"")))
    })
    public ResourceDTO getResourceForNode(
            @Parameter(description = "Node database id, or foreignSource:foreignId.", required = true,
                    example = "loopback-lab:lb-001")
            @PathParam("nodeCriteria") final String nodeCriteria,
            @Parameter(description = "How many levels of children to include. The default of -1 walks the whole subtree.",
                    example = "-1")
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "No node found with criteria '{}'.", nodeCriteria);
        }

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource found for node with id {}.", "" + node.getId());
        }

        return ResourceDTO.fromResource(resource, depth);
    }

    /**
     * Selects nodes and parts of there resource information.
     * <ul>
     *   <li>If no {@code nodeSubresources} criteria is given then all subresources are returned but their nested content is not included.</li>
     *   <li>If no {@code stringProperties} criteria is given then all string properties are included.</li>
     * </ul>
     *
     * @param nodes Comma separated list of node ids; both, database ids and foreign ids are supported
     * @param filterRules Comma separated list of rule names for selecting nodes
     * @param nodeSubresources Comma separated list of subresource names (the part after the dot)
     * @param stringProperties Comma separated list of property names
     * @return
     */
    @GET
    @Path("select")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "Select node resources and prune them",
            description = """
        Return node resources for a set of nodes, pruned to the parts asked for. Nodes come from `nodes`,
        from `filterRules`, or from both; the two sets are merged.

        The node resource always comes back with its own attribute maps blanked. What happens below it
        depends on `nodeSubresources`:

        - omitted: every sub-resource is listed, but stripped of its children and attribute maps;
        - given: only sub-resources whose id ends in `.<name>` are kept, and those keep their
          `stringPropertyAttributes`.

        `stringProperties` narrows the string properties on the kept sub-resources to the named ones. It has
        no effect unless `nodeSubresources` is also given.

        Every parameter is a comma-separated list, and unknown node ids are dropped silently rather than
        reported. The response is a bare JSON array, not the usual count/offset envelope.""",
            operationId = "selectResources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The selected node resources. An empty array when nothing matched.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    array = @ArraySchema(schema = @Schema(implementation = ResourceDTO.class)),
                                    examples = @ExampleObject(value = """
                    [
                      {
                        "id": "node[loopback-lab:lb-004]",
                        "label": "loopback-004",
                        "name": "loopback-lab:lb-004",
                        "link": "element/node.jsp?node=loopback-lab:lb-004",
                        "typeLabel": "Node",
                        "parentId": null,
                        "children": {
                          "totalCount": 1,
                          "count": 1,
                          "offset": 0,
                          "resource": [
                            {
                              "id": "node[loopback-lab:lb-004].responseTime[127.0.0.4]",
                              "label": "Response Time for 127.0.0.4",
                              "name": "127.0.0.4",
                              "link": "element/interface.jsp?node=loopback-lab:lb-004&intf=127.0.0.4",
                              "typeLabel": "Response Time",
                              "parentId": "node[loopback-lab:lb-004]",
                              "stringPropertyAttributes": {}
                            }
                          ]
                        }
                      }
                    ]""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    array = @ArraySchema(schema = @Schema(implementation = ResourceDTO.class)))
                    })
    })
    public List<ResourceDTO> select(
            @Parameter(description = "Comma-separated node ids. Database ids and foreignSource:foreignId pairs may be mixed.",
                    example = "1,loopback-lab:lb-002")
            @DefaultValue("") @QueryParam("nodes") String nodes,
            @Parameter(description = "Comma-separated filter rule names, each resolved to the node ids it matches. Merged with `nodes`.",
                    example = "Routers")
            @DefaultValue("") @QueryParam("filterRules") String filterRules,
            @Parameter(description = "Comma-separated sub-resource names, matched against the part of the id after the last dot. Omit to list all sub-resources without their contents.",
                    example = "responseTime[127.0.0.4]")
            @DefaultValue("") @QueryParam("nodeSubresources") String nodeSubresources,
            @Parameter(description = "Comma-separated string-property names to keep on the selected sub-resources. Only applies when `nodeSubresources` is given.",
                    example = "sysName")
            @DefaultValue("") @QueryParam("stringProperties") String stringProperties
    ) {
        var allNodeIds = Stream.of(nodes.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toSet());

        var ruleNodeIds = Stream.of(filterRules.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .flatMap(s -> m_filterDao.getNodeMap(s).keySet().stream().map(String::valueOf))
                .collect(Collectors.toSet());

        allNodeIds.addAll(ruleNodeIds);

        var subresourceNames = Stream.of(nodeSubresources.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .distinct()
                .collect(Collectors.toList());

        var stringPropertyNames = Stream.of(stringProperties.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toSet());

        var resources = allNodeIds.stream().sorted()
                .map(nodeId -> {
                    if (nodeId.contains(":")) {
                        var node = m_nodeDao.get(nodeId);
                        return node != null ? m_resourceDao.getResourceForNode(node) : null;
                    } else {
                        return m_resourceDao.getResourceById(ResourceId.get("node", nodeId));
                    }
                })
                .filter(r -> r != null)
                .map(r -> ResourceDTO.fromResource(r, -1))
                .map(nodeResource -> {
                    nodeResource.setRrdGraphAttributes(null);
                    nodeResource.setStringPropertyAttributes(null);
                    nodeResource.setExternalValueAttributes(null);
                    if (nodeResource.getChildren() == null) {
                        return nodeResource;
                    }
                    // prune the node resource
                    // -> include only selected parts if selections are available
                    if (subresourceNames.isEmpty()) {
                        // no subresources are selected
                        // -> include all subresources but strip their content
                        // -> information about children but not about their content is required for browsing
                        for (var c : nodeResource.getChildren()) {
                            c.setChildren(null);
                            c.setRrdGraphAttributes(null);
                            c.setStringPropertyAttributes(null);
                            c.setExternalValueAttributes(null);
                        }
                    } else {
                        // include only those children that match any of the given subresource names
                        for (var subresourceIterator = nodeResource.getChildren().iterator(); subresourceIterator.hasNext(); ) {
                            var subresource = subresourceIterator.next();
                            if (subresourceNames.stream().anyMatch(name -> subresource.getId().endsWith("." + name))) {
                                // this subresource is selected
                                // -> we are not interested in its children, rrd graph attributes, and external value attributes
                                subresource.setChildren(null);
                                subresource.setRrdGraphAttributes(null);
                                subresource.setExternalValueAttributes(null);
                                // check if specific string properties or external values are selected
                                // -> in that case only include these ones
                                // -> otherwise include all
                                if (!stringPropertyNames.isEmpty()) {
                                    // -> include only selected stringProperties and externalValues
                                    for (var iter = subresource.getStringPropertyAttributes().keySet().iterator(); iter.hasNext(); ) {
                                        var s = iter.next();
                                        if (!stringPropertyNames.contains(s)) {
                                            iter.remove();
                                        }
                                    }
                                }
                            } else {
                                // subresource is not selected
                                // -> remove it from result
                                subresourceIterator.remove();
                            }
                        }
                    }
                    return nodeResource;
                });

        return resources.collect(Collectors.toList());
    }

    @POST
    @Path("generateId")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Store a list of resource ids under a generated key",
            description = """
        Store a list of resource ids in the JSON store and return the key they were filed under.

        The key is a UUID derived from the JSON form of the list, so the same list always produces the same
        key and re-posting it overwrites the entry with identical content. Order matters: a reordered list is
        a different key.

        The response body is a JSON string, quotes included. The ids are not validated.""",
            operationId = "generateResourceIdKey"
    )
    @RequestBody(
            required = true,
            description = "The resource ids to store, as a JSON array of strings.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(type = "string")),
                    examples = @ExampleObject(value = """
                    [
                      "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                      "node[loopback-lab:lb-002].responseTime[127.0.0.2]"
                    ]"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The key the list was stored under, as a JSON string.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "\"3c9721e3-1213-37d4-b7d3-71407ad60eb7\"")))
    })
    public Response saveResourcesWithId(String[] resources) {

        String resourcesInJson = m_gson.toJson(resources);
        // The generated key will be same for a given input.
        String key = UUID.nameUUIDFromBytes(resourcesInJson.getBytes()).toString();
        m_jsonStore.put(key, resourcesInJson, RESOURCE_IDS_CONTEXT);
        String jsonKey = m_gson.toJson(key);
        return Response.ok(jsonKey).build();

    }

}
