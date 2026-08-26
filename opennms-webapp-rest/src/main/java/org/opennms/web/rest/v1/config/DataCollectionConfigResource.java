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
package org.opennms.web.rest.v1.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component
public class DataCollectionConfigResource implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfigResource.class);

    @Resource(name="dataCollectionConfigDao")
    private DataCollectionConfigDao m_dataCollectionConfigDao;

    public void setDataCollectionConfigDao(final DataCollectionConfigDao dao) {
        m_dataCollectionConfigDao = dao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_dataCollectionConfigDao, "DataCollectionConfigDao must be set!");
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the SNMP data collection configuration",
            description = """
                    Returns datacollection-config.xml with every `datacollection-group` include already
                    resolved and flattened into the owning `snmp-collection`. The response is very large:
                    on a stock installation it is several hundred kilobytes of JSON, because it carries
                    every shipped MIB group, table, resource type and systemDef.

                    Prefer `/config/datacollection/status` for a summary, or
                    `/config/datacollection/lookup` to see what a single agent would be collected for.""",
            operationId = "getDataCollectionConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The resolved data collection configuration. The example is abbreviated to one group and one MIB group.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = DatacollectionConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "snmp-collection": [
                                                {
                                                  "name": "default",
                                                  "datacollection-group": [
                                                    {
                                                      "name": "default-all",
                                                      "resourceType": [],
                                                      "table": [],
                                                      "group": [
                                                        {
                                                          "name": "mib2-icmp",
                                                          "mibObj": [
                                                            {
                                                              "oid": ".1.3.6.1.2.1.5.2",
                                                              "instance": "0",
                                                              "alias": "icmpInErrors",
                                                              "type": "counter"
                                                            }
                                                          ]
                                                        }
                                                      ],
                                                      "systemDef": []
                                                    }
                                                  ]
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = DatacollectionConfig.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The data collection DAO holds no root configuration. Bodiless.")
    })
    public Response getDataCollectionConfiguration() throws ConfigurationResourceException {
        LOG.debug("getDatacollectionConfigurationForLocation()");

        final DatacollectionConfig dcc = m_dataCollectionConfigDao.getRootDataCollection();
        if (dcc == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(dcc.toDataCollectionConfig()).build();
    }

    /**
     * Diagnostic endpoint: returns the MIB objects that would be collected
     * for a given sysoid, IP address, collection name, and interface type.
     *
     * Example: /rest/config/datacollection/lookup?sysoid=.1.3.6.1.4.1.8072.3.2.10&address=127.0.0.1
     */
    @GET
    @Path("/lookup")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Resolve the MIB objects for one agent",
            description = """
                    Diagnostic endpoint. Resolves `sysoid`, `address`, `collection` and `ifType` through the
                    same systemDef matching that collectd uses, and returns the MIB objects that would be
                    collected. Nothing is queried over SNMP and no node has to exist: the address is used
                    only for the address-range tests inside the systemDef rules.

                    Leave `ifType` at its default of -1 for the node-level objects. Pass a positive IANA
                    interface type (6 for ethernetCsmacd, for example) to also include the interface-level
                    objects that apply to that type.""",
            operationId = "lookupDataCollectionMibObjects")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The resolved MIB objects. The example is abbreviated to one object.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionLookupResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "sysoid": ".1.3.6.1.4.1.8072.3.2.10",
                                      "address": "127.0.0.1",
                                      "collection": "default",
                                      "ifType": -1,
                                      "matchedObjectCount": 191,
                                      "objects": [
                                        {
                                          "group": "mib2-tcp",
                                          "oid": ".1.3.6.1.2.1.6.5",
                                          "alias": "tcpActiveOpens",
                                          "type": "Counter32",
                                          "instance": "0"
                                        }
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "`sysoid` was absent or empty. The body is a plain string, even though the response is labelled application/json.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "sysoid parameter is required")))
    })
    public Response lookupMibObjects(
            @Parameter(description = "sysObjectID of the agent to resolve, including the leading dot.", required = true, example = ".1.3.6.1.4.1.8072.3.2.10")
            @QueryParam("sysoid") final String sysoid,
            @Parameter(description = "Interface address used for the address-range tests in the systemDef rules.", example = "127.0.0.1")
            @QueryParam("address") @DefaultValue("127.0.0.1") final String address,
            @Parameter(description = "Name of the snmp-collection to resolve against, as named in datacollection-config.xml.", example = "default")
            @QueryParam("collection") @DefaultValue("default") final String collection,
            @Parameter(description = "IANA interface type. -1 returns the node-level objects only.", example = "-1")
            @QueryParam("ifType") @DefaultValue("-1") final int ifType) {

        if (sysoid == null || sysoid.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("sysoid parameter is required").build();
        }

        final List<MibObject> mibObjects = m_dataCollectionConfigDao.getMibObjectList(collection, sysoid, address, ifType);

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("sysoid", sysoid);
        result.put("address", address);
        result.put("collection", collection);
        result.put("ifType", ifType);
        result.put("matchedObjectCount", mibObjects.size());
        result.put("objects", mibObjects.stream().map(obj -> {
            final Map<String, String> m = new LinkedHashMap<>();
            m.put("group", obj.getGroupName());
            m.put("oid", obj.getOid());
            m.put("alias", obj.getAlias());
            m.put("type", obj.getType());
            m.put("instance", obj.getInstance());
            return m;
        }).collect(Collectors.toList()));

        return Response.ok(result).build();
    }

    /**
     * Diagnostic endpoint: returns a summary of the in-memory config.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Summarize the loaded data collection configuration",
            description = """
                    Diagnostic endpoint. Returns counts drawn from the data collection configuration
                    currently held in memory, without serializing the configuration itself. Useful for
                    confirming that a datacollection.d file was picked up after a reload.

                    `lastUpdate` is epoch milliseconds. The internal `__resource_type_collection` entry is
                    filtered out of `snmpCollections` but is still counted by the `available*` totals.""",
            operationId = "getDataCollectionConfigStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary of the loaded configuration. `availableCollectionGroups` is abbreviated in the example.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionStatusResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "availableCollectionGroups": ["MIB2", "Cisco", "Net-SNMP"],
                                      "availableSystemDefs": 216,
                                      "availableMibGroups": 328,
                                      "configuredResourceTypes": 195,
                                      "lastUpdate": 1787685418848,
                                      "snmpCollections": [
                                        {
                                          "name": "default",
                                          "storageFlag": "select",
                                          "rrdStep": 300,
                                          "groups": 324,
                                          "systemDefs": 215,
                                          "resourceTypes": 194
                                        }
                                      ]
                                    }""")))
    })
    public Response getConfigStatus() {
        final Map<String, Object> status = new LinkedHashMap<>();
        status.put("availableCollectionGroups", m_dataCollectionConfigDao.getAvailableDataCollectionGroups());
        status.put("availableSystemDefs", m_dataCollectionConfigDao.getAvailableSystemDefs().size());
        status.put("availableMibGroups", m_dataCollectionConfigDao.getAvailableMibGroups().size());
        status.put("configuredResourceTypes", m_dataCollectionConfigDao.getConfiguredResourceTypes().size());
        status.put("lastUpdate", m_dataCollectionConfigDao.getLastUpdate());

        final DatacollectionConfig config = m_dataCollectionConfigDao.getRootDataCollection();
        if (config != null) {
            status.put("snmpCollections", config.getSnmpCollections().stream()
                    .filter(c -> !"__resource_type_collection".equals(c.getName()))
                    .map(c -> {
                        final Map<String, Object> coll = new LinkedHashMap<>();
                        coll.put("name", c.getName());
                        coll.put("storageFlag", c.getSnmpStorageFlag());
                        coll.put("rrdStep", c.getRrd() != null ? c.getRrd().getStep() : null);
                        coll.put("groups", c.getGroups() != null ? c.getGroups().getGroups().size() : 0);
                        coll.put("systemDefs", c.getSystems() != null ? c.getSystems().getSystemDefs().size() : 0);
                        coll.put("resourceTypes", c.getResourceTypes().size());
                        return coll;
                    }).collect(Collectors.toList()));
        }

        return Response.ok(status).build();
    }
}
