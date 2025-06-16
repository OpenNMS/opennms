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
package org.opennms.web.rest.v2.bsm.model.edge;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;

public class IpServiceEdgeResponseDTOMarshalTest extends MarshalAndUnmarshalTest<IpServiceEdgeResponseDTO> {

    public IpServiceEdgeResponseDTOMarshalTest(Class<IpServiceEdgeResponseDTO> clazz, IpServiceEdgeResponseDTO sampleObject, String sampleJson, String sampleXml) {
        super(clazz, sampleObject, sampleJson, sampleXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        IpServiceResponseDTO ipService = new IpServiceResponseDTO();
        ipService.setId(17);
        ipService.setIpAddress("1.1.1.1");
        ipService.setNodeLabel("dummy");
        ipService.setServiceName("ICMP");
        ipService.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", "17"));

        MapFunctionDTO mapFunctionDTO = new MapFunctionDTO();
        mapFunctionDTO.getProperties().put("key1", "value1");
        mapFunctionDTO.setType("SetTo");

        IpServiceEdgeResponseDTO edge = new IpServiceEdgeResponseDTO();
        edge.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "edges", "1"));
        edge.setIpService(ipService);
        edge.setOperationalStatus(Status.WARNING);
        edge.getReductionKeys().add("key1");
        edge.getReductionKeys().add("key2");
        edge.setFriendlyName("ip-service-friendly-name");
        edge.setWeight(20);
        edge.setMapFunction(mapFunctionDTO);
        edge.setId(1);

        return Arrays.asList(new Object[][]{{
                IpServiceEdgeResponseDTO.class,
                edge,
                "{" +
                "  \"id\" : 1," +
                "  \"operational-status\" : \"WARNING\"," +
                "  \"map-function\" : {" +
                "       \"type\" : \"SetTo\"," +
                "       \"properties\" : {" +
                "           \"key1\" : \"value1\"" +
                "       }" +
                "   }," +
                "  \"location\" : \"/api/v2/business-services/edges/1\"," +
                "  \"reduction-keys\" : [" +
                "       \"key1\", \"key2\""+
                "   ]," +
                "  \"weight\" : 20," +
                "  \"ip-service\" : {" +
                "       \"id\" : 17," +
                "       \"location\" : \"/api/v2/business-services/ip-services/17\"," +
                "       \"ip-address\" : \"1.1.1.1\"," +
                "       \"node-label\" : \"dummy\"," +
                "       \"service-name\" : \"ICMP\"" +
                "  }," +
                "  \"friendly-name\" : \"ip-service-friendly-name\"" +
                "}",
                "<ip-service-edge>\n" +
                "   <id>1</id>\n" +
                "   <operational-status>WARNING</operational-status>\n" +
                "   <map-function>\n" +
                "      <type>SetTo</type>\n" +
                "      <properties>\n" +
                "         <entry>\n" +
                "            <key>key1</key>\n" +
                "            <value>value1</value>\n" +
                "         </entry>\n" +
                "      </properties>\n" +
                "   </map-function>\n" +
                "   <location>/api/v2/business-services/edges/1</location>\n" +
                "   <reduction-keys>\n" +
                "      <reduction-key>key1</reduction-key>\n" +
                "      <reduction-key>key2</reduction-key>\n" +
                "   </reduction-keys>\n" +
                "   <weight>20</weight>\n" +
                "   <ip-service>\n" +
                "      <id>17</id>\n" +
                "      <service-name>ICMP</service-name>\n" +
                "      <node-label>dummy</node-label>\n" +
                "      <ip-address>1.1.1.1</ip-address>\n" +
                "      <location>/api/v2/business-services/ip-services/17</location>\n" +
                "   </ip-service>\n" +
                "   <friendly-name>ip-service-friendly-name</friendly-name>\n" +
                "</ip-service-edge>"
        }});
    }
}
