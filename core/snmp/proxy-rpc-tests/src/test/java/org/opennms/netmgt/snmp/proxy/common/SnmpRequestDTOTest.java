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
package org.opennms.netmgt.snmp.proxy.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

import com.google.common.collect.Lists;

public class SnmpRequestDTOTest extends XmlTestNoCastor<SnmpRequestDTO> {

    public SnmpRequestDTOTest(SnmpRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpWalkRequest(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-request description=\"some random oids\" location=\"dc2\">\n" +
                      "<agent>\n" +
                         "<authPassPhrase>0p3nNMSv3</authPassPhrase>\n" +
                         "<authProtocol>MD5</authProtocol>\n" +
                         "<maxRepetitions>2</maxRepetitions>\n" +
                         "<maxRequestSize>65535</maxRequestSize>\n" +
                         "<maxVarsPerPdu>10</maxVarsPerPdu>\n" +
                         "<port>161</port>\n" +
                         "<privPassPhrase>0p3nNMSv3</privPassPhrase>\n" +
                         "<privProtocol>DES</privProtocol>\n" +
                         "<readCommunity>public</readCommunity>\n" +
                         "<retries>0</retries>\n" +
                         "<securityLevel>1</securityLevel>\n" +
                         "<securityName>opennmsUser</securityName>\n" +
                         "<timeout>3000</timeout>\n" +
                         "<version>1</version>\n" +
                         "<versionAsString>v1</versionAsString>\n" +
                         "<writeCommunity>private</writeCommunity>\n" +
                         "<address>192.168.0.2</address>\n" +
                      "</agent>\n" +
                      "<get correlation-id=\"44\">\n" +
                         "<oid>.1.3.6.1.2.1.3.1.3.0</oid>\n" +
                      "</get>\n" +
                      "<walk correlation-id=\"42\" max-repetitions=\"4\">\n" +
                         "<oid>.1.3.6.1.2.1.4.34.1.3</oid>\n" +
                         "<oid>.1.3.6.1.2.1.4.34.1.5</oid>\n" +
                         "<oid>.1.3.6.1.2.1.4.34.1.4</oid>\n" +
                      "</walk>\n" +
                      "<walk correlation-id=\"43\" instance=\".0\">\n" +
                         "<oid>.1.3.6.1.2.1.3.1.3</oid>\n" +
                      "</walk>\n" +
                    "</snmp-request>"
                }
        });
    }

    private static SnmpRequestDTO getSnmpWalkRequest() throws UnknownHostException {
        final SnmpAgentConfig agent = new SnmpAgentConfig();
        agent.setAddress(InetAddress.getByName("192.168.0.2"));

        final SnmpWalkRequestDTO walkRequest = new SnmpWalkRequestDTO();
        walkRequest.setCorrelationId("42");
        walkRequest.setMaxRepetitions(4);
        walkRequest.setOids(Lists.newArrayList(
                SnmpObjId.get(SnmpObjId.get(".1.3.6.1.2.1.4.34.1"), "3"),
                SnmpObjId.get(SnmpObjId.get(".1.3.6.1.2.1.4.34.1"), "5"),
                SnmpObjId.get(SnmpObjId.get(".1.3.6.1.2.1.4.34.1"), "4")
        ));

        SnmpWalkRequestDTO singleInstanceWalkRequest = new SnmpWalkRequestDTO();
        singleInstanceWalkRequest.setCorrelationId("43");
        singleInstanceWalkRequest.setInstance(SnmpInstId.INST_ZERO);
        singleInstanceWalkRequest.setOids(Lists.newArrayList(
                SnmpObjId.get(".1.3.6.1.2.1.3.1.3")
        ));

        final SnmpGetRequestDTO getRequest = new SnmpGetRequestDTO();
        getRequest.setCorrelationId("44");
        getRequest.setOids(Lists.newArrayList(
                SnmpObjId.get(SnmpObjId.get(".1.3.6.1.2.1.3.1.3"), "0")
        ));

        final SnmpRequestDTO snmpRequest = new SnmpRequestDTO();
        snmpRequest.setDescription("some random oids");
        snmpRequest.setLocation("dc2");
        snmpRequest.setAgent(agent);
        snmpRequest.setWalkRequests(Lists.newArrayList(walkRequest, singleInstanceWalkRequest));
        snmpRequest.setGetRequests(Lists.newArrayList(getRequest));
        return snmpRequest;
    }
}
