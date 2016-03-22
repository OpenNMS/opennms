/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
