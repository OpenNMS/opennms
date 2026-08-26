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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Read-only API for retrieving graph definitions and determining
 * the list of supported graphs on a particular resource.
 *
 * @author jwhite
 */
@Component("graphRestService")
@Path("graphs")
@Tag(name = "Graphs", description = """
        Graphs API: the prefabricated graph definitions from `snmp-graph.properties` and the
        `snmp-graph.properties.d` directory, and which of them apply to a given resource.

        A definition is identified by its name and carries the RRDtool command template used to render it,
        the columns it needs and the resource types it applies to. These operations only report definitions;
        rendering happens elsewhere.

        Whether a definition applies to a resource is decided by the attributes the resource actually has, so
        two nodes of the same type can offer different graphs.""")
public class GraphRestService extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private GraphDao m_graphDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "List all prefabricated graph names",
            description = """
        List the names of every prefabricated graph definition known to the running instance, sorted. This is
        the whole catalogue, not the subset that applies to any particular resource, and on a default install
        it runs to well over a thousand entries.""",
            operationId = "getGraphNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The graph names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = GraphNameCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1763,
                      "count": 1763,
                      "offset": 0,
                      "name": [
                        "mib2.HCbits",
                        "http-8080"
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = GraphNameCollection.class))
                    })
    })
    public GraphNameCollection getGraphNames() {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getAllPrefabGraphs()) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("{graphName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "Get one prefabricated graph definition",
            description = """
        Return a single graph definition: its title, the columns it reads, the RRDtool command template and
        the resource types it applies to. `{rrd1}`, `{rrd2}` and similar placeholders in `command` are
        substituted at render time.""",
            operationId = "getGraphByName"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The graph definition.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = PrefabGraph.class),
                                    examples = @ExampleObject(value = """
                    {
                      "name": "mib2.HCbits",
                      "title": "Bits In/Out (High Speed)",
                      "columns": [
                        "ifHCInOctets",
                        "ifHCOutOctets"
                      ],
                      "command": "--title=\"Bits In/Out (High Speed)\" --vertical-label=\"Bits per second\" DEF:octIn={rrd1}:ifHCInOctets:AVERAGE DEF:octOut={rrd2}:ifHCOutOctets:AVERAGE",
                      "externalValues": [],
                      "propertiesValues": [],
                      "order": 4222,
                      "types": [
                        "interfaceSnmp"
                      ],
                      "description": null,
                      "width": null,
                      "height": null,
                      "suppress": [
                        "mib2.bits"
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = PrefabGraph.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No definition with that name exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No graph with name 'nope' found.")))
    })
    public PrefabGraph getGraphByName(
            @Parameter(description = "Graph definition name, as listed by `GET /graphs`.", required = true,
                    example = "mib2.HCbits")
            @PathParam("graphName") final String graphName) {
        try {
            return m_graphDao.getPrefabGraph(graphName);
        } catch (ObjectRetrievalFailureException e) {
            throw getException(Status.NOT_FOUND, "No graph with name '{}' found.", graphName);
        }
    }

    @GET
    @Path("for/{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "List the graphs that apply to a resource",
            description = """
        Return the names of the graph definitions that apply to one resource, decided from the attributes the
        resource actually has. The path segment is a resource id and has to be percent-encoded, since ids
        contain `[`, `]` and often `:`. An id whose grammar does not parse fails with 500 rather than 400.""",
            operationId = "getGraphNamesForResource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The applicable graph names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = GraphNameCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "name": [
                        "http-8080"
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = GraphNameCollection.class))
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
    public GraphNameCollection getGraphNamesForResource(
            @Parameter(description = "Percent-encoded resource id.", required = true,
                    example = "node[loopback-lab:lb-001].responseTime[127.0.0.1]")
            @PathParam("resourceId") final String resourceId) {
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.fromString(resourceId));
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }
        return getGraphNamesForResource(resource);
    }

    private GraphNameCollection getGraphNamesForResource(final OnmsResource resource) {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getPrefabGraphsForResource(resource)) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    @Operation(
            summary = "Get a node's resource tree with its graph definitions",
            description = """
        Return the node's resource tree with each resource decorated with the graph names that apply to it,
        alongside the full definitions of every graph named anywhere in the tree. That makes one request
        enough to draw a node's whole graph page.

        `nodeCriteria` is either the database node id or `foreignSource:foreignId`. A criteria string that is
        neither numeric nor contains a colon fails with 500 while trying to parse it as a number, rather than
        with 404.

        Definitions are listed once each under `prefab-graphs`, keyed `prefab-graph` inside the envelope,
        however many resources in the tree reference them.""",
            operationId = "getGraphResourcesForNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node resource tree and the graph definitions it uses.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = GraphResourceDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "resource": {
                        "id": "node[loopback-lab:lb-004]",
                        "label": "loopback-004",
                        "name": "loopback-lab:lb-004",
                        "link": "element/node.jsp?node=loopback-lab:lb-004",
                        "typeLabel": "Node",
                        "parentId": null,
                        "graphNames": [],
                        "children": {
                          "totalCount": 1,
                          "count": 1,
                          "offset": 0,
                          "resource": [
                            {
                              "id": "node[loopback-lab:lb-004].responseTime[127.0.0.4]",
                              "label": "Response Time for 127.0.0.4",
                              "name": "127.0.0.4",
                              "typeLabel": "Response Time",
                              "parentId": "node[loopback-lab:lb-004]",
                              "graphNames": [
                                "http-8080"
                              ],
                              "rrdGraphAttributes": {
                                "http-8080": {
                                  "name": "http-8080",
                                  "relativePath": "response/127.0.0.4",
                                  "rrdFile": "http-8080.rrd"
                                }
                              }
                            }
                          ]
                        }
                      },
                      "prefab-graphs": {
                        "totalCount": 1,
                        "count": 1,
                        "offset": 0,
                        "prefab-graph": [
                          {
                            "name": "http-8080",
                            "title": "HTTP-8080",
                            "columns": [
                              "http-8080"
                            ],
                            "order": 7,
                            "types": [
                              "responseTime",
                              "perspectiveResponseTime"
                            ],
                            "width": null,
                            "height": null,
                            "suppress": []
                          }
                        ]
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = GraphResourceDTO.class))
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
    public GraphResourceDTO getGraphResourcesForNode(
            @Parameter(description = "Node database id, or foreignSource:foreignId.", required = true,
                    example = "loopback-lab:lb-004")
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

        final ResourceVisitor visitor = new ResourceVisitor(this);
        final ResourceDTO resourceDTO = ResourceDTO.fromResource(resource, depth);
        visitor.visit(resourceDTO);

        return new GraphResourceDTO(resourceDTO, visitor.getGraphs());
    }

    @XmlRootElement(name = "graph-resource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class GraphResourceDTO {
        @XmlElement(name="resource")
        private ResourceDTO m_resource;

        @XmlElement(name="prefab-graphs")
        private PrefabGraphCollection m_prefabGraphs;

        @SuppressWarnings("unused")
        private GraphResourceDTO() {
            super();
        }

        public GraphResourceDTO(final ResourceDTO resource, final PrefabGraphCollection graphs) {
            m_resource = resource;
            m_prefabGraphs = graphs;
        }
    }

    public static final class ResourceVisitor {
        private final Map<String,PrefabGraph> m_graphs = Maps.newLinkedHashMap();
        private final GraphRestService m_service;

        public ResourceVisitor(GraphRestService service) {
            m_service = service;
        }

        public void visit(final ResourceDTO resource) {
            // first, decorate the DTO with the list of graph names
            final GraphNameCollection graphNames = m_service.getGraphNamesForResource(resource.getResource());
            resource.setGraphNames(graphNames.getObjects());

            // then, get the prefab graphs for these graph names if we don't have them already
            for (final String graphName : graphNames) {
                if (!m_graphs.containsKey(graphName)) {
                    m_graphs.put(graphName, m_service.getGraphByName(graphName));
                }
            }

            // finally, recurse if necessary
            if (resource.getChildren() != null) {
                for (final ResourceDTO r : resource.getChildren()) {
                    this.visit(r);
                }
            }
        }

        public PrefabGraphCollection getGraphs() {
            return new PrefabGraphCollection(m_graphs.values());
        }
    }
}
