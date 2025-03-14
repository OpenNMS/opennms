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
import java.util.HashMap;
import java.util.Map;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;

public class MapFunctionDTOMarshalTest extends MarshalAndUnmarshalTest<MapFunctionDTO> {
    public MapFunctionDTOMarshalTest(Class<MapFunctionDTO> type, MapFunctionDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        MapFunctionDTO dto = new MapFunctionDTO();
        dto.setType("Increase");
        Map<String, String> properties = new HashMap<>();
        properties.put("status", "some-status");
        properties.put("some-property", "some-value");
        dto.setProperties(properties);

        return Arrays.asList(new Object[][]{{
                MapFunctionDTO.class,
                dto,
                "{\n" +
                "  \"type\" : \"Increase\",\n" +
                "  \"properties\" : {\n" +
                "    \"status\" : \"some-status\",\n" +
                "    \"some-property\" : \"some-value\"\n" +
                "  }\n" +
                "}\n",
                "<map-function>\n" +
                "   <type>Increase</type>\n" +
                "   <properties>\n" +
                "       <entry>\n" +
                "           <key>status</key>\n" +
                "           <value>some-status</value>\n" +
                "       </entry>\n" +
                "       <entry>\n" +
                "           <key>some-property</key>\n" +
                "           <value>some-value</value>\n" +
                "       </entry>\n" +
                "   </properties>\n" +
                "</map-function>"
        }});
    }
}
