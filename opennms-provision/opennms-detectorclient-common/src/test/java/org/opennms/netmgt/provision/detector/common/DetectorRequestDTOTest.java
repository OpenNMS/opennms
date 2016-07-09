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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DetectorRequestDTOTest extends XmlTestNoCastor<DetectorRequestDTO> {

    public DetectorRequestDTOTest(DetectorRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    } 
    
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDetectorRequest(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<detector-request location=\"MINION\" serviceName=\"ICMP\" address=\"localhost\">\n" +
                      "<attributes><entry> <key>port</key> <value>8980</value> </entry> </attributes> \n" +
                    "</detector-request>"
                }
        });
    }

    public static DetectorRequestDTO getDetectorRequest() {
        DetectorRequestDTO dto = new DetectorRequestDTO();
        dto.setAddress("localhost");
        dto.setLocation("MINION");
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("port", "8980");
        dto.setAttributes(properties);
        dto.setServiceName("ICMP");
        return dto;
    }
}
