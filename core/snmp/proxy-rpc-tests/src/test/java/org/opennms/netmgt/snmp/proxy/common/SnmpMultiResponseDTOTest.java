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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

public class SnmpMultiResponseDTOTest extends XmlTestNoCastor<SnmpMultiResponseDTO> {

    public SnmpMultiResponseDTOTest(SnmpMultiResponseDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpMultiResponse(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-response>\n" +
                        "<response correlation-id=\"42\">\n" +
                            "<result>\n" +
                              "<base>.1.3.6.1.2</base>\n" +
                              "<instance>1.3.6.1.2.1.4.34.1.3.1.2.3.4</instance>\n" +
                              "<value type=\"70\">Cg==</value>\n" +
                            "</result>\n" +
                        "</response>\n" +
                    "</snmp-response>"
                }
        });
    }

    private static SnmpMultiResponseDTO getSnmpMultiResponse() {
        final SnmpValueFactory snmpValueFactory = new Snmp4JValueFactory();
        final SnmpResult result = new SnmpResult(
                SnmpObjId.get(".1.3.6.1.2"),
                new SnmpInstId(".1.3.6.1.2.1.4.34.1.3.1.2.3.4"),
                snmpValueFactory.getCounter64(BigInteger.TEN));
        final SnmpResponseDTO responseDTO = new SnmpResponseDTO();
        responseDTO.setCorrelationId("42");
        responseDTO.getResults().add(result);

        final SnmpMultiResponseDTO multiResponseDTO = new SnmpMultiResponseDTO();
        multiResponseDTO.getResponses().add(responseDTO);
        return multiResponseDTO;
    }
}
