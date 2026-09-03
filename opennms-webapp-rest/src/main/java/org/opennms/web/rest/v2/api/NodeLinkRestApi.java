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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opennms.web.rest.model.v2.BridgeElementNodeDTO;
import org.opennms.web.rest.model.v2.BridgeLinkNodeDTO;
import org.opennms.web.rest.model.v2.CdpElementNodeDTO;
import org.opennms.web.rest.model.v2.CdpLinkNodeDTO;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.opennms.web.rest.model.v2.IsisElementNodeDTO;
import org.opennms.web.rest.model.v2.IsisLinkNodeDTO;
import org.opennms.web.rest.model.v2.LldpElementNodeDTO;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.model.v2.OspfElementNodeDTO;
import org.opennms.web.rest.model.v2.OspfLinkNodeDTO;

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
 * Read-only projections of the link-discovery (enlinkd) tables for a single
 * node, shaped for the node link pages rather than for machine consumption:
 * the fields are pre-rendered display strings, some of which embed relative
 * JSP URLs.
 */
@Path("enlinkd")
@Tag(name = "Enlinkd", description = """
        Enlinkd API.

        Every operation is addressed by a node criteria and returns the link-discovery rows enlinkd has
        persisted for that node. The response fields are display strings, not raw table columns: a port
        reads `GigabitEthernet0/1(ifindex:1)(interfaceName:Gi0/1)`, and the `*Url` fields are JSP paths
        (`element/snmpinterface.jsp?node=2&ifindex=1`) relative to the OpenNMS web root.

        The create and last-poll timestamps arrive pre-formatted as locale-dependent strings such as
        `8/17/26, 5:20:39 PM` rather than as epoch milliseconds or ISO-8601. On this instance the
        separator before AM/PM is U+202F (narrow no-break space), not a plain space.

        Where discovery has populated nothing, the `_links` operations answer 200 with an empty array
        and the LLDP, CDP, OSPF and IS-IS `_elems` operations answer 204. `bridge_elems` returns a
        collection rather than a single element, so it answers 200 with an empty array.""")
public interface NodeLinkRestApi {

    /** Shared so the twelve node-criteria parameters cannot drift apart. */
    String NODE_CRITERIA_DESC = """
            Either a numeric node id (`2`) or a `foreignSource:foreignId` pair (`loopback-lab:lb-001`).
            The pair form is recognised by the presence of a colon; anything else is parsed as an
            integer.""";

    String NODE_CRITERIA_500 = """
            The criteria contains no colon and is not an integer, so parsing it as a node id fails.
            A well-formed criteria that matches nothing answers 404 instead.""";

    @GET
    @Path("{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's all types of links",
            description = """
        Every link type and element enlinkd holds for one node, in a single response. The link
        collections are always present, empty where that protocol has nothing for the node. The
        single-element fields (`lldpElementNode`, `cdpElementNode`, `ospfElementNode`,
        `isisElementNode`) are omitted from the JSON entirely when the node has no such element, rather
        than being emitted as null.""",
            operationId = "NodeLinkRestApiGetAllTypesOfLinks", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All link types held for the node.",
                    content = @Content(schema = @Schema(implementation = EnlinkdDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "lldpLinkNodes": [
                        {
                          "lldpLocalPort": "GigabitEthernet0/1(ifindex:1)(interfaceName:Gi0/1)",
                          "lldpLocalPortUrl": "element/snmpinterface.jsp?node=2&ifindex=1",
                          "lldpRemChassisId": "loopback-004(macAddress:ch-001)",
                          "lldpRemChassisIdUrl": "element/linkednode.jsp?node=1",
                          "lldpRemInfo": "loopback-004",
                          "ldpRemPort": "GigabitEthernet0/2(interfaceName:Gi0/2)",
                          "lldpCreateTime": "8/17/26, 5:20:39 PM",
                          "lldpLastPollTime": "8/17/26, 5:20:39 PM"
                        }
                      ],
                      "bridgeLinkNodes": [],
                      "cdpLinkNodes": [],
                      "ospfLinkNodes": [
                        {
                          "ospfLocalPort": "lo()(ifindex:1)(10.10.1.2)",
                          "ospfLocalPortUrl": "element/snmpinterface.jsp?node=2&ifindex=1",
                          "ospfRemRouterId": "loopback-004(router id:10.255.0.1)",
                          "ospfRemRouterUrl": "element/linkednode.jsp?node=1",
                          "ospfRemPort": "(10.10.1.1)",
                          "ospfRemPortUrl": "element/interface.jsp?node=1&intf=10.10.1.1",
                          "ospfLinkInfo": "(mask:255.255.255.252)",
                          "ospfLinkCreateTime": "8/17/26, 5:20:39 PM",
                          "ospfLinkLastPollTime": "8/17/26, 5:20:39 PM"
                        }
                      ],
                      "isisLinkNodes": [],
                      "lldpElementNode": {
                        "lldpChassisId": "(macAddress:ch-002)",
                        "lldpSysName": "loopback-001",
                        "lldpCreateTime": "8/17/26, 5:20:39 PM",
                        "lldpLastPollTime": "8/17/26, 5:20:39 PM"
                      },
                      "bridgeElementNodes": [],
                      "ospfElementNode": {
                        "ospfRouterId": "10.255.0.2",
                        "ospfVersionNumber": 2,
                        "ospfAdminStat": "enabled",
                        "ospfCreateTime": "8/17/26, 5:20:39 PM",
                        "ospfLastPollTime": "8/17/26, 5:20:39 PM"
                      }
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"router-1\"")))
    })
    EnlinkdDTO getEnlinkd(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                          @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("lldp_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's LLDP Link",
            description = """
        LLDP neighbour rows for one node. `ldpRemPort` is spelled without a leading `l` in both JSON
        and XML.""",
            operationId = "NodeLinkRestApiGetNodeLLDPLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LLDP links held for the node, empty when LLDP found none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LldpLinkNodeDTO.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "lldpLocalPort": "GigabitEthernet0/1001(ifindex:2)(interfaceName:Gi0/1001)",
                        "lldpLocalPortUrl": "element/snmpinterface.jsp?node=1011&ifindex=2",
                        "lldpRemChassisId": "scale-core-001(macAddress:sc-001001)",
                        "lldpRemChassisIdUrl": "element/linkednode.jsp?node=1001",
                        "lldpRemInfo": "scale-core-001",
                        "ldpRemPort": "GigabitEthernet0/1011(interfaceName:Gi0/1011)",
                        "lldpCreateTime": "8/18/26, 1:16:57 PM",
                        "lldpLastPollTime": "8/18/26, 1:16:57 PM"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<LldpLinkNodeDTO> getLldpLinks(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "1011")
                                       @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("bridge_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's bridge Link",
            description = """
        Bridge-forwarding links for one node. Each entry covers one local port and carries the remote
        ends reachable through it as a nested collection.""",
            operationId = "NodeLinkRestApiGetNodeBridgeLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bridge links held for the node, empty when bridge discovery found none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BridgeLinkNodeDTO.class)),
                            examples = @ExampleObject(value = "[]"))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<BridgeLinkNodeDTO> getBridgeLinks(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                           @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("cdp_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's CDP Link", description = "CDP neighbour rows for one node.",
            operationId = "NodeLinkRestApiGetNodeCDPLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CDP links held for the node, empty when CDP found none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CdpLinkNodeDTO.class)),
                            examples = @ExampleObject(value = "[]"))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<CdpLinkNodeDTO> getCdpLinks(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                     @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("ospf_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's OSPF Link",
            description = "OSPF adjacency rows for one node, one entry per local interface with a discovered neighbour.",
            operationId = "NodeLinkRestApiGetNodeOSPFLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OSPF links held for the node, empty when OSPF found none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OspfLinkNodeDTO.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "ospfLocalPort": "lo()(ifindex:1)(10.10.1.2)",
                        "ospfLocalPortUrl": "element/snmpinterface.jsp?node=2&ifindex=1",
                        "ospfRemRouterId": "loopback-004(router id:10.255.0.1)",
                        "ospfRemRouterUrl": "element/linkednode.jsp?node=1",
                        "ospfRemPort": "(10.10.1.1)",
                        "ospfRemPortUrl": "element/interface.jsp?node=1&intf=10.10.1.1",
                        "ospfLinkInfo": "(mask:255.255.255.252)",
                        "ospfLinkCreateTime": "8/17/26, 5:20:39 PM",
                        "ospfLinkLastPollTime": "8/17/26, 5:20:39 PM"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<OspfLinkNodeDTO> getOspfLinks(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                       @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("isis_links/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's IS-IS Link", description = "IS-IS adjacency rows for one node.",
            operationId = "NodeLinkRestApiGetNodeISISLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IS-IS links held for the node, empty when IS-IS found none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = IsisLinkNodeDTO.class)),
                            examples = @ExampleObject(value = "[]"))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<IsisLinkNodeDTO> getIsisLinks(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                       @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("lldp_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's LLDP element",
            description = "The node's own LLDP identity: chassis id and system name.",
            operationId = "NodeLinkRestApiGetElementLLDPLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's LLDP element.",
                    content = @Content(schema = @Schema(implementation = LldpElementNodeDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "lldpChassisId": "(macAddress:ch-002)",
                      "lldpSysName": "loopback-001",
                      "lldpCreateTime": "8/17/26, 5:20:39 PM",
                      "lldpLastPollTime": "8/17/26, 5:20:39 PM"
                    }"""))),
            @ApiResponse(responseCode = "204", description = "The node exists but has no LLDP element."),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    LldpElementNodeDTO getLldpElem(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "loopback-lab:lb-001")
                                   @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("bridge_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's bridge element",
            description = """
        The node's bridge identities, one per discovered VLAN. A device with no VLAN table yields a single
        entry whose `vlan` is null. This operation returns a collection, so a node with no bridge element
        answers 200 with an empty array rather than 204.""",
            operationId = "NodeLinkRestApiGetElementBridgeLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's bridge elements, empty when it has none.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BridgeElementNodeDTO.class)),
                            examples = @ExampleObject(value = "[]"))),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    List<BridgeElementNodeDTO> getBridgeElem(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                             @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("cdp_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's CDP element",
            description = "The node's own CDP identity: global device id and run state.",
            operationId = "NodeLinkRestApiGetElementCDPLinkByNodeId" , tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's CDP element.",
                    content = @Content(schema = @Schema(implementation = CdpElementNodeDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "cdpGlobalRun": "true",
                      "cdpGlobalDeviceId": "core-sw-01",
                      "cdpCreateTime": "8/17/26, 5:20:39 PM",
                      "cdpLastPollTime": "8/17/26, 5:20:39 PM"
                    }"""))),
            @ApiResponse(responseCode = "204", description = "The node exists but has no CDP element."),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    CdpElementNodeDTO getCdpElem(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                 @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("ospf_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's OSPF element",
            description = "The node's own OSPF identity: router id, protocol version and admin state.",
            operationId = "NodeLinkRestApiGetElementOSPFLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's OSPF element.",
                    content = @Content(schema = @Schema(implementation = OspfElementNodeDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "ospfRouterId": "10.255.0.2",
                      "ospfVersionNumber": 2,
                      "ospfAdminStat": "enabled",
                      "ospfCreateTime": "8/17/26, 5:20:39 PM",
                      "ospfLastPollTime": "8/17/26, 5:20:39 PM"
                    }"""))),
            @ApiResponse(responseCode = "204", description = "The node exists but has no OSPF element."),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    OspfElementNodeDTO getOspfElem(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                   @PathParam("node_criteria") String nodeCriteria);

    @GET
    @Path("isis_elems/{node_criteria}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a node's IS-IS element",
            description = "The node's own IS-IS identity: system id and administrative state.",
            operationId = "NodeLinkRestApiGetElementISISLinkByNodeId", tags = {"Enlinkd"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's IS-IS element.",
                    content = @Content(schema = @Schema(implementation = IsisElementNodeDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "isisSysID": "0102.0304.0506",
                      "isisSysAdminState": "on",
                      "isisCreateTime": "8/17/26, 5:20:39 PM",
                      "isisLastPollTime": "8/17/26, 5:20:39 PM"
                    }"""))),
            @ApiResponse(responseCode = "204", description = "The node exists but has no IS-IS element."),
            @ApiResponse(responseCode = "404", description = "No node matches the criteria.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Not able to find node with criteria : 99999999"))),
            @ApiResponse(responseCode = "500", description = NODE_CRITERIA_500,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    IsisElementNodeDTO getIsisElem(@Parameter(description = NODE_CRITERIA_DESC, required = true, example = "2")
                                   @PathParam("node_criteria") String nodeCriteria);

}
