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
package org.opennms.web.rest.v2.bsm.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;

public class IpServiceResponseDTOMarshalTest extends MarshalAndUnmarshalTest<IpServiceResponseDTO> {

    public IpServiceResponseDTOMarshalTest(Class<IpServiceResponseDTO> clazz, IpServiceResponseDTO sampleObject, String sampleJson, String sampleXml) {
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

        return Arrays.asList(new Object[][]{{
                IpServiceResponseDTO.class,
                ipService,
                "{" +
                "   \"id\" : 17," +
                "   \"location\" : \"/api/v2/business-services/ip-services/17\"," +
                "   \"ip-address\" : \"1.1.1.1\"," +
                "   \"node-label\" : \"dummy\"," +
                "   \"service-name\" : \"ICMP\"" +
                "}",
                "<ip-service>\n" +
                "   <id>17</id>\n" +
                "   <service-name>ICMP</service-name>\n" +
                "   <node-label>dummy</node-label>\n" +
                "   <ip-address>1.1.1.1</ip-address>\n" +
                "   <location>/api/v2/business-services/ip-services/17</location>\n" +
                "</ip-service>"
        }});
    }
}
