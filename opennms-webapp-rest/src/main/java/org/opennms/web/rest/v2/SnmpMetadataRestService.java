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
package org.opennms.web.rest.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.snmpmetadata.SnmpMetadataBase;
import org.opennms.netmgt.model.snmpmetadata.SnmpMetadataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("snmpmetadata")
@Transactional
@Tag(name = "SnmpMetadata", description = "SNMP metadata API")
public class SnmpMetadataRestService {

    /** The node DAO. */
    @Autowired
    private NodeDao nodeDao;

    @GET
    @Path("{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get the SNMP metadata tree of a node",
            description = """
        Return the node's metadata entries in the `snmp` context, reshaped into a tree. The flat
        `key = value` entries are parsed on the way out: a key of `a.b` becomes nested objects, and a
        key of `t[i].k` becomes a table `t` with an entry indexed `i`. Nothing is stored in tree form,
        so the result is derived on every call.

        The `snmp` context is written by provisiond's SNMP metadata collection, not through the metadata
        API, which refuses any context that does not begin with `X-`. A node with no `snmp` metadata is
        a 200 carrying an empty tree.

        The `id` field is a process-wide counter assigned while the tree is built, so the same node
        yields different `id` values on successive calls. It is not a database identifier and the XML
        representation omits it. Table entries come back in map iteration order rather than sorted by
        index.""",
            operationId = "SnmpMetadataRestServiceGETSNMPMetaDataByNodId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The node's SNMP metadata tree, rooted at an object named `snmp`.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpMetadataObject.class),
                                    examples = {
                                            @ExampleObject(name = "populated", value = """
                    {
                      "id": 2,
                      "name": "snmp",
                      "snmp-metadata-object": [
                        {
                          "id": 3,
                          "name": "sysDescr",
                          "snmp-metadata-object": [],
                          "snmp-metadata-value": [
                            {"id": 4, "name": "sysDescr", "value": "Linux apidoc 5.15.0"}
                          ],
                          "snmp-metadata-table": []
                        }
                      ],
                      "snmp-metadata-value": [],
                      "snmp-metadata-table": [
                        {
                          "id": 5,
                          "name": "ifTable",
                          "snmp-metadata-entry": [
                            {
                              "id": 6,
                              "index": "2",
                              "snmp-metadata-value": [
                                {"id": 7, "name": "ifSpeed", "value": "1000000000"},
                                {"id": 8, "name": "ifDescr", "value": "eth0"}
                              ]
                            }
                          ]
                        }
                      ]
                    }"""),
                                            @ExampleObject(name = "no snmp metadata", value = """
                    {"id": 0, "name": "snmp", "snmp-metadata-object": [], "snmp-metadata-value": [], "snmp-metadata-table": []}""")
                                    }),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpMetadataObject.class),
                                    examples = @ExampleObject(value = """
                    <snmp-metadata-object name="snmp">
                      <snmp-metadata-object name="sysDescr">
                        <snmp-metadata-value name="sysDescr" value="Linux apidoc 5.15.0"/>
                      </snmp-metadata-object>
                      <snmp-metadata-table name="ifTable">
                        <snmp-metadata-entry index="2">
                          <snmp-metadata-value name="ifSpeed" value="1000000000"/>
                          <snmp-metadata-value name="ifDescr" value="eth0"/>
                        </snmp-metadata-entry>
                      </snmp-metadata-table>
                    </snmp-metadata-object>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response getSnmpMetadata(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.",
                    example = "257")
            @PathParam("nodeCriteria") String nodeCriteria) {
        final OnmsNode node = nodeDao.get(nodeCriteria);
        if (node == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final SnmpMetadataBase snmpMetadataBase = SnmpMetadataObject.fromOnmsMetadata(node.getMetaData(), "snmp");
        return Response.ok(snmpMetadataBase).build();
    }
}
