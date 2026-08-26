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

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component("snmpConfigurationResource")
public class SnmpConfigurationResource {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigurationResource.class);

    @Resource(name="snmp-config.xml")
    ConfigurationResource<SnmpConfig> m_snmpConfigResource;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the SNMP agent configuration",
            description = """
                    Returns the whole of snmp-config.xml: the top-level defaults followed by the per-address
                    and per-range `definition` entries, and the named `profiles` if any are configured.

                    Community strings, authentication and privacy passphrases are returned in the clear,
                    exactly as they are stored. On an installation that keeps SNMP configuration in the
                    database rather than in the file, the response reflects the database contents.

                    Unset defaults come back as JSON nulls and are omitted entirely from the XML form.""",
            operationId = "getSnmpConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current SNMP configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "definition": [],
                                              "profiles": null,
                                              "version": "v2c",
                                              "readCommunity": "public",
                                              "writeCommunity": null,
                                              "timeout": 1800,
                                              "retry": 1,
                                              "port": null,
                                              "maxVarsPerPdu": null,
                                              "maxRepetitions": null,
                                              "securityLevel": null,
                                              "securityName": null
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpConfig.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8"?>
                                            <snmp-config xmlns="http://xmlns.opennms.org/xsd/config/snmp"
                                                         version="v2c" read-community="public" timeout="1800" retry="1"/>"""))
                    })
    })
    public Response getSnmpConfiguration() throws ConfigurationResourceException {
        return Response.ok(m_snmpConfigResource.get()).build();
    }
}
