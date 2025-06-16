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
