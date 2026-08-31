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
import org.opennms.netmgt.config.TrapdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component("trapdConfigurationResource")
public class TrapdConfigurationResource {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(TrapdConfigurationResource.class);
    
    @Resource(name="trapd-configuration.xml")
    ConfigurationResource<TrapdConfig> m_trapdConfigResource;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the trapd configuration",
            description = """
                    Returns the contents of trapd-configuration.xml, including the SNMPv3 users trapd will
                    accept traps from. A `threads` value of 0 means trapd sizes the pool from the available
                    processor count.""",
            operationId = "getTrapdConfigurationV1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current trapd configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = org.opennms.netmgt.config.trapd.TrapdConfiguration.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "threads": 0,
                                              "snmp-trap-address": "*",
                                              "snmp-trap-port": 10162,
                                              "new-suspect-on-trap": false,
                                              "include-raw-message": false,
                                              "queue-size": 10000,
                                              "batch-size": 1000,
                                              "batch-interval": 500,
                                              "snmpv3-user": [],
                                              "use-address-from-varbind": null
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = org.opennms.netmgt.config.trapd.TrapdConfiguration.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8"?>
                                            <trapd-configuration xmlns="http://xmlns.opennms.org/xsd/config/trapd"
                                                                 snmp-trap-address="*" snmp-trap-port="10162"
                                                                 new-suspect-on-trap="false" include-raw-message="false"
                                                                 threads="0" queue-size="10000" batch-size="1000"
                                                                 batch-interval="500"/>"""))
                    })
    })
    public Response getTrapdConfiguration() throws ConfigurationResourceException {
        return Response.ok(m_trapdConfigResource.get()).build();
    }

}
