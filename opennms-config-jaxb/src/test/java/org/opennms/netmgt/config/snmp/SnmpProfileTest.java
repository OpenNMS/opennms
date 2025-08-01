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
package org.opennms.netmgt.config.snmp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SnmpProfileTest extends XmlTestNoCastor<SnmpProfile> {

    public SnmpProfileTest(final SnmpProfile sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {

        SnmpProfile snmpProfile = new SnmpProfile(1, // port
                2, // retry
                100, // timeout
                "readCommunity", "writeCommunity",
                "proxyHost",
                "v2c", // version
                4, // max-vars-per-pdu
                5, // max-repetitions
                484, // max-request-size
                "securityName",
                3, // security-level
                "authPassphrase",
                "MD5", // auth-protocol
                "engineId", "contextEngineId", "contextName",
                "privacyPassphrase", "DES", // privacy-protocol
                "enterpriseId",
                "profile-1",
                "nodeLabel LIKE 'Minion%'");
        return Arrays.asList(new Object[][]{{
                snmpProfile,
                "<profile " + "port=\"1\" " + "retry=\"2\" "
                        + "  timeout=\"100\" "
                        + "  read-community=\"readCommunity\" "
                        + "  write-community=\"writeCommunity\" "
                        + "  proxy-host=\"proxyHost\" "
                        + "  version=\"v2c\" "
                        + "  max-vars-per-pdu=\"4\" "
                        + "  max-repetitions=\"5\" "
                        + "  max-request-size=\"484\" "
                        + "  security-name=\"securityName\" "
                        + "  security-level=\"3\" "
                        + "  auth-passphrase=\"authPassphrase\" "
                        + "  auth-protocol=\"MD5\" "
                        + "  engine-id=\"engineId\" "
                        + "  context-engine-id=\"contextEngineId\" "
                        + "  context-name=\"contextName\" "
                        + "  privacy-passphrase=\"privacyPassphrase\" "
                        + "  privacy-protocol=\"DES\" "
                        + "  enterprise-id=\"enterpriseId\">"
                        + "  <label>profile-1</label>"
                        + "  <filter>nodeLabel LIKE 'Minion%'</filter>"
                        + "</profile>\n",
                "target/classes/xsds/snmp-config.xsd"}});
    }
}
