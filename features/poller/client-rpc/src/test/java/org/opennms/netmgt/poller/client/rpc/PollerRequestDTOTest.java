/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.client.rpc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class PollerRequestDTOTest extends XmlTestNoCastor<PollerRequestDTO> {

    public PollerRequestDTOTest(PollerRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            {
                getPollerRequestWithString(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                  "<attribute key=\"port\" value=\"18980\"/>\n" +
                "</poller-request>"
            },
            {
                getPollerRequestWithStringAsObject(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                  "<attribute key=\"port\" value=\"18980\"/>\n" +
                "</poller-request>"
            },
            {
                getPollerRequestWithObject(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                  "<attribute key=\"nested\">" +
                    "<attribute key=\"x\" value=\"y\"/>" +
                  "</attribute>" +
                "</poller-request>"
            },
            {
                getPollerRequestWithAgentConfig(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                  "<attribute key=\"agent\">" +
                    "<snmpAgentConfig>" +
                        "<authPassPhrase>0p3nNMSv3</authPassPhrase>" +
                        "<authProtocol>MD5</authProtocol>" +
                        "<maxRepetitions>2</maxRepetitions>" +
                        "<maxRequestSize>65535</maxRequestSize>" +
                        "<maxVarsPerPdu>10</maxVarsPerPdu>" +
                        "<port>161</port>" +
                        "<privPassPhrase>0p3nNMSv3</privPassPhrase>" +
                        "<privProtocol>DES</privProtocol>" +
                        "<readCommunity>public</readCommunity>" +
                        "<retries>0</retries>" +
                        "<securityLevel>1</securityLevel>" +
                        "<securityName>opennmsUser</securityName>" +
                        "<timeout>3000</timeout>" +
                        "<version>1</version>" +
                        "<versionAsString>v1</versionAsString>" +
                        "<writeCommunity>private</writeCommunity>" +
                    "</snmpAgentConfig>" +
                  "</attribute>" +
                "</poller-request>"
            }
        });
    }
    public static PollerRequestDTO getPollerRequestWithString() throws UnknownHostException {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addAttribute("port", "18980");
        return dto;
    }

    public static PollerRequestDTO getPollerRequestWithStringAsObject() throws UnknownHostException {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        // Casting the String to an Object will use a different code path
        dto.addAttribute("port", (Object)"18980");
        return dto;
    }

    public static PollerRequestDTO getPollerRequestWithObject() throws UnknownHostException {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addAttribute("nested", new PollerAttributeDTO("x", "y"));
        return dto;
    }

    public static PollerRequestDTO getPollerRequestWithAgentConfig() throws UnknownHostException {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addAttribute("agent", new SnmpAgentConfig());
        return dto;
    }
}
