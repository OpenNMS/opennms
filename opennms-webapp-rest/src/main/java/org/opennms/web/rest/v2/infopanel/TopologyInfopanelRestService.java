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
package org.opennms.web.rest.v2.infopanel;

import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Serves operator-configured topology info-panel content for a node: the
 * rendered {@code $OPENNMS_HOME/etc/infopanel/} Jinjava templates as
 * {@code [{title, order, html}]}. The new Vue topology Inspector appends these
 * below its native fields, so the existing template investment carries over.
 *
 * <p>Read-only and open to any authenticated user via the default
 * {@code /api/v2/**} security rules. {@code @Transactional} keeps a Hibernate
 * session open so templates can traverse lazy node/resource associations.
 */
@Component
@Path("topology/infopanel")
@Tag(name = "Topology", description = "Topology map APIs")
@Produces(MediaType.APPLICATION_JSON)
public class TopologyInfopanelRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    // Optional: templates that embed metrics use it; absent installs degrade
    // gracefully (those template sections are skipped, not fatal to the rest).
    @Autowired(required = false)
    private MeasurementsService m_measurementsService;

    private volatile InfoPanelRenderer m_renderer;

    private static final String ITEMS_EXAMPLE = """
            [
              {
                "title": "SNMP Attributes",
                "order": 10,
                "html": "<table><tr><td>sysName</td><td>loopback-001</td></tr></table>"
              }
            ]""";

    private static final String EMPTY_RESULT_NOTE = """
            An empty array is returned when `$OPENNMS_HOME/etc/infopanel/` holds no `*.html`
            templates, or when every template set `visible` to false for this subject.""";

    @GET
    @Transactional(readOnly = true)
    @Operation(summary = "Get info-panel items for a node",
            description = """
        Renders every `*.html` Jinjava template in `$OPENNMS_HOME/etc/infopanel/` against the node and
        returns the ones that set `visible` to true, sorted ascending by `order`. A template that fails
        to render is skipped and logged; it does not fail the request.

        """ + EMPTY_RESULT_NOTE,
            operationId = "getTopologyInfopanelForNode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rendered items, empty when no template produced one.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = InfoPanelItem.class)),
                            examples = @ExampleObject(value = ITEMS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "`nodeId` was not supplied.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "A nodeId query parameter is required"))),
            @ApiResponse(responseCode = "404", description = """
                    No node with that id. A non-numeric `nodeId` also lands here, as an empty-bodied 404
                    from the parameter conversion rather than this message.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No node with id 99999999")))
    })
    public List<InfoPanelItem> getForNode(@Parameter(description = "Numeric id of the node to render for.",
                                                  required = true, example = "2")
                                          @QueryParam("nodeId") final Integer nodeId) {
        if (nodeId == null) {
            throw webException(Response.Status.BAD_REQUEST, "A nodeId query parameter is required");
        }
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw webException(Response.Status.NOT_FOUND, "No node with id " + nodeId);
        }
        return renderer().renderForNode(node);
    }

    /**
     * Edge-scoped panels for a link between two nodes: templates render with
     * an {@code edge} context (see {@link EdgeInfo}). Port names and protocol
     * are optional -- they come from a link's discovery binding when present.
     */
    @GET
    @javax.ws.rs.Path("edge")
    @Transactional(readOnly = true)
    @Operation(summary = "Get info-panel items for a link between two nodes",
            description = """
        Same template set and same visible/title/order contract as the node operation, rendered with an
        `edge` context instead: the two endpoint nodes, their port labels, the SNMP interface each port
        resolved to, and the discovery protocol.

        Port labels are matched against each node's SNMP interfaces by `ifName` first, then `ifDescr`.
        A port that matches nothing leaves the endpoint's `snmpInterface` and `ifIndex` null.

        """ + EMPTY_RESULT_NOTE,
            operationId = "getTopologyInfopanelForEdge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rendered items, empty when no template produced one.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = InfoPanelItem.class)),
                            examples = @ExampleObject(value = ITEMS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "`sourceNodeId` or `targetNodeId` was not supplied.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "sourceNodeId and targetNodeId query parameters are required"))),
            @ApiResponse(responseCode = "404", description = "Either endpoint id does not resolve to a node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No node with id 99999999")))
    })
    public List<InfoPanelItem> getForEdge(@Parameter(description = "Numeric id of the link's source node.",
                                                  required = true, example = "2")
                                          @QueryParam("sourceNodeId") final Integer sourceNodeId,
                                          @Parameter(description = "Numeric id of the link's target node.",
                                                  required = true, example = "1")
                                          @QueryParam("targetNodeId") final Integer targetNodeId,
                                          @Parameter(description = """
                                                  Port label on the source node, matched against its SNMP
                                                  interfaces by ifName then ifDescr.""",
                                                  example = "Gi0/1")
                                          @QueryParam("sourcePort") final String sourcePort,
                                          @Parameter(description = "Port label on the target node, matched the same way.",
                                                  example = "Gi0/2")
                                          @QueryParam("targetPort") final String targetPort,
                                          @Parameter(description = "Discovery protocol the link came from, passed through to the templates.",
                                                  example = "LLDP")
                                          @QueryParam("protocol") final String protocol) {
        if (sourceNodeId == null || targetNodeId == null) {
            throw webException(Response.Status.BAD_REQUEST, "sourceNodeId and targetNodeId query parameters are required");
        }
        final OnmsNode source = m_nodeDao.get(sourceNodeId);
        final OnmsNode target = m_nodeDao.get(targetNodeId);
        if (source == null || target == null) {
            throw webException(Response.Status.NOT_FOUND,
                    "No node with id " + (source == null ? sourceNodeId : targetNodeId));
        }
        final EdgeInfo edge = new EdgeInfo(protocol,
                new EdgeInfo.Port(source, sourcePort, resolveSnmpInterface(source, sourcePort)),
                new EdgeInfo.Port(target, targetPort, resolveSnmpInterface(target, targetPort)));
        return renderer().renderForEdge(edge);
    }

    /**
     * Match a persisted port label against the node's SNMP interfaces by
     * ifName, then ifDescr (the two labels discovery writes). Null when the
     * port is unknown or nothing matches -- templates handle a null
     * {@code snmpInterface}/{@code ifIndex} themselves.
     */
    private static OnmsSnmpInterface resolveSnmpInterface(final OnmsNode node, final String port) {
        if (port == null || port.isBlank()) {
            return null;
        }
        return node.getSnmpInterfaces().stream()
                .filter(s -> port.equals(s.getIfName()) || port.equals(s.getIfDescr()))
                .findFirst()
                .orElse(null);
    }

    private InfoPanelRenderer renderer() {
        if (m_renderer == null) {
            synchronized (this) {
                if (m_renderer == null) {
                    final java.nio.file.Path dir = Paths.get(System.getProperty("opennms.home", "."), "etc", "infopanel");
                    m_renderer = new InfoPanelRenderer(m_nodeDao, m_resourceDao, m_measurementsService, dir);
                }
            }
        }
        return m_renderer;
    }

    private static javax.ws.rs.WebApplicationException webException(final Response.Status status, final String message) {
        return new javax.ws.rs.WebApplicationException(
                Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
