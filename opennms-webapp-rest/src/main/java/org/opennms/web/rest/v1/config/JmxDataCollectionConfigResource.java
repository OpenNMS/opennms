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
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component("jmxDataCollectionConfigResource")
public class JmxDataCollectionConfigResource implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(JmxDataCollectionConfigResource.class);

    @Resource(name = "jmxDataCollectionConfigDao")
    private JMXDataCollectionConfigDao m_jmxDataCollectionConfigDao;

    public void setJmxDataCollectionConfigDao(final JMXDataCollectionConfigDao dao) {
        m_jmxDataCollectionConfigDao = dao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_jmxDataCollectionConfigDao, "JmxDataCollectionConfigDao must be set!");
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the JMX data collection configuration",
            description = """
                    Returns jmx-datacollection-config.xml as the JMX collection DAO holds it, with every
                    `import-mbeans` include already resolved into the owning collection. The shipped default
                    serializes to roughly 100 kB of JSON.

                    `rrdRepository` is an absolute path on the OpenNMS server. Each `jmx-collection` carries
                    its own `rrd` step and RRA definitions.""",
            operationId = "getJmxDataCollectionConfig")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current JMX data collection configuration. The example is abbreviated to a single collection, MBean and attribute.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = JmxDatacollectionConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "rrdRepository": "/opt/opennms/share/rrd/snmp/",
                                              "jmx-collection": [
                                                {
                                                  "name": "jmx-kafka",
                                                  "rrd": {
                                                    "step": 300,
                                                    "rra": [
                                                      "RRA:AVERAGE:0.5:1:2016",
                                                      "RRA:AVERAGE:0.5:12:1488",
                                                      "RRA:AVERAGE:0.5:288:366",
                                                      "RRA:MAX:0.5:288:366",
                                                      "RRA:MIN:0.5:288:366"
                                                    ]
                                                  },
                                                  "mbeans": [
                                                    {
                                                      "name": "JVM Memory",
                                                      "objectname": "java.lang:type=OperatingSystem",
                                                      "keyfield": null,
                                                      "exclude": null,
                                                      "key-alias": null,
                                                      "resource-type": null,
                                                      "attrib": [
                                                        {
                                                          "name": "FreePhysicalMemorySize",
                                                          "alias": "FreeMemory",
                                                          "type": "gauge",
                                                          "maxval": null,
                                                          "minval": null
                                                        }
                                                      ],
                                                      "comp-attrib": []
                                                    }
                                                  ]
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = JmxDatacollectionConfig.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The JMX data collection DAO holds no configuration. Bodiless.")
    })
    public Response getJmxDataCollectionConfig() throws ConfigurationResourceException {
        LOG.debug("getJmxDataCollectionConfigurationForLocation()");

        final JmxDatacollectionConfig jmxDataCollectionConfig = m_jmxDataCollectionConfigDao.getConfig();

        if (jmxDataCollectionConfig == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(jmxDataCollectionConfig).build();
    }
}
