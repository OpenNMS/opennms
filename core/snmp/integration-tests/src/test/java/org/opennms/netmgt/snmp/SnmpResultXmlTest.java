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
package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

/**
 * This test is maintained outside of the API project since it needs
 * access to an implementation of the {@link SnmpValueFactory}.
 *
 * @author jwhite
 */
public class SnmpResultXmlTest extends XmlTestNoCastor<SnmpResult> {

    public SnmpResultXmlTest(SnmpResult sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpResult(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-result>\n" +
                       "<base>.1.3.6.1.2</base>\n" +
                       "<instance>1.3.6.1.2.1.4.34.1.3.1.2.3.4</instance>\n" +
                       "<value type=\"70\">Cg==</value>\n" +
                    "</snmp-result>"
                }
        });
    }

    private static SnmpResult getSnmpResult() {
        final SnmpValueFactory snmpValueFactory = new Snmp4JValueFactory();
        final SnmpResult result = new SnmpResult(
                SnmpObjId.get(".1.3.6.1.2"),
                new SnmpInstId(".1.3.6.1.2.1.4.34.1.3.1.2.3.4"),
                snmpValueFactory.getCounter64(BigInteger.TEN));
        return result;
    }
}
