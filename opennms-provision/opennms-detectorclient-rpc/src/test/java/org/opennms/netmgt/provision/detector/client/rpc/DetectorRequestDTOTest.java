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

package org.opennms.netmgt.provision.detector.client.rpc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.provision.detector.client.rpc.DetectorRequestDTO;

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
                "<detector-request location=\"MINION\" class-name=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" address=\"127.0.0.1\">\n" +
                  "<detector-attribute key=\"port\">8980</detector-attribute>\n" +
                  "<runtime-attribute key=\"password\">foo</runtime-attribute>\n" +
                "</detector-request>"
            }
        });
    }

    public static DetectorRequestDTO getDetectorRequest() throws UnknownHostException {
        DetectorRequestDTO dto = new DetectorRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.provision.detector.icmp.IcmpDetector");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addDetectorAttribute("port", "8980");
        dto.addRuntimeAttribute("password", "foo");
        return dto;
    }
}
