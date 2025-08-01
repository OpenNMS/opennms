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

public class ChildEdgeResponseDTOMarshalTest extends MarshalAndUnmarshalTest<ChildEdgeResponseDTO> {

    public ChildEdgeResponseDTOMarshalTest(Class<ChildEdgeResponseDTO> clazz, ChildEdgeResponseDTO sampleObject, String sampleJson, String sampleXml) {
        super(clazz, sampleObject, sampleJson, sampleXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        MapFunctionDTO mapFunctionDTO = new MapFunctionDTO();
        mapFunctionDTO.getProperties().put("key1", "value1");
        mapFunctionDTO.setType("SetTo");

        ChildEdgeResponseDTO edge = new ChildEdgeResponseDTO();
        edge.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "edges", "1"));
        edge.setOperationalStatus(Status.WARNING);
        edge.getReductionKeys().add("key1");
        edge.getReductionKeys().add("key2");
        edge.setMapFunction(mapFunctionDTO);
        edge.setChildId(127L);
        edge.setWeight(20);
        edge.setId(1);

        return Arrays.asList(new Object[][]{{
                ChildEdgeResponseDTO.class,
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
                "       \"location\" : \"/api/v2/business-services/edges/1\"," +
                "  \"reduction-keys\" : [" +
                "       \"key1\", \"key2\"" +
                "   ]," +
                "  \"weight\" : 20," +
                "  \"child-id\" : 127" +
                "}",
                "<child-edge>\n" +
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
                "   <child-id>127</child-id>" +
                "</child-edge>"
        }});
    }
}
