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

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

import com.google.common.collect.Lists;

public class SnmpSetRequestDTOTest extends XmlTestNoCastor<SnmpSetRequestDTO> {

    public SnmpSetRequestDTOTest(SnmpSetRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpSetRequest(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-set-request correlation-id=\"set-123\">\n" +
                      "<oid>.1.3.6.1.2.1.1.6.0</oid>\n" +
                      "<oid>.1.3.6.1.2.1.1.5.0</oid>\n" +
                      "<value type=\"4\">VGVzdCBMb2NhdGlvbg==</value>\n" +
                      "<value type=\"4\">VGVzdCBTeXNOYW1l</value>\n" +
                    "</snmp-set-request>"
                }
        });
    }

    private static SnmpSetRequestDTO getSnmpSetRequest() {
        final SnmpValueFactory snmpValueFactory = new Snmp4JValueFactory();

        final SnmpSetRequestDTO setRequest = new SnmpSetRequestDTO();
        setRequest.setCorrelationId("set-123");
        setRequest.setOids(Lists.newArrayList(
                SnmpObjId.get(".1.3.6.1.2.1.1.6.0"), // sysLocation
                SnmpObjId.get(".1.3.6.1.2.1.1.5.0")  // sysName
        ));
        setRequest.setValues(Lists.newArrayList(
                snmpValueFactory.getOctetString("Test Location".getBytes()),
                snmpValueFactory.getOctetString("Test SysName".getBytes())
        ));

        return setRequest;
    }
}
