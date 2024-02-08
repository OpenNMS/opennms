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

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.xml.JaxbUtils;

public class SnmpConfigTest extends XmlTestNoCastor<SnmpConfig> {

    public SnmpConfigTest(final SnmpConfig sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final List<Definition> definitionList = new ArrayList<>();

        final Range range = new Range();
        range.setBegin("192.168.0.1");
        range.setEnd("192.168.0.255");

        final Definition def = new Definition();
        def.setVersion("v3");
        def.setReadCommunity("public");
        def.setWriteCommunity("private");
        def.addRange(range);
        def.addSpecific("192.168.1.1");
        def.addIpMatch("10.0.0.*");
        definitionList.add(def);
        SnmpConfig snmpConfig = new SnmpConfig(
                1, // port
                2, // retry
                3, // timeout
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
                "enterpriseId", definitionList);
        SnmpProfiles snmpProfiles = new SnmpProfiles();
        SnmpProfile snmpProfile = new SnmpProfile(1, // port
                2, // retry
                3, // timeout
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
                "profile1",
                "nodeLabel LIKE 'Minion%'");
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpProfile = new SnmpProfile(18980, // port
                5, // retry
                300, // timeout
                "readCommunity", "writeCommunity",
                "proxyHost",
                "v3", // version
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
                "profile2",
                "nodeLabel LIKE 'Minion%'");
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpConfig.setSnmpProfiles(snmpProfiles);

        return Arrays.asList(new Object[][] {
                {
                    new SnmpConfig(
                                   1, // port
                                   2, // retry
                                   3, // timeout
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
                                   "enterpriseId", definitionList),
                                   "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" "
                                           + "  timeout=\"3\" "
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
                                           + "  <definition "
                                           + "    read-community=\"public\" "
                                           + "    write-community=\"private\" "
                                           + "    version=\"v3\">" + "    <range "
                                           + "      begin=\"192.168.0.1\" "
                                           + "      end=\"192.168.0.255\"/>"
                                           + "    <specific>192.168.1.1</specific>"
                                           + "    <ip-match>10.0.0.*</ip-match>"
                                           + "  </definition>" + "</snmp-config>\n",
                "target/classes/xsds/snmp-config.xsd" },
                {
                    new SnmpConfig(
                                   1, // port
                                   2, // retry
                                   3, // timeout
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
                                   "enterpriseId", definitionList),
                                   "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" "
                                           + "  port=\"1\" " + "  retry=\"2\" "
                                           + "  timeout=\"3\" "
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
                                           + "  <definition "
                                           + "    read-community=\"public\" "
                                           + "    write-community=\"private\" "
                                           + "    version=\"v3\">" + "    <range "
                                           + "      begin=\"192.168.0.1\" "
                                           + "      end=\"192.168.0.255\"/>"
                                           + "    <specific>192.168.1.1</specific>"
                                           + "    <ip-match>10.0.0.*</ip-match>"
                                           + "  </definition>" + "</snmp-config>\n",
                "target/classes/xsds/snmp-config.xsd" },
                {
                    snmpConfig,
                        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" "
                        + "  port=\"1\" " + "  retry=\"2\" "
                        + "  timeout=\"3\" "
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
                        + "  <definition "
                        + "    read-community=\"public\" "
                        + "    write-community=\"private\" "
                        + "    version=\"v3\">" + "    <range "
                        + "      begin=\"192.168.0.1\" "
                        + "      end=\"192.168.0.255\"/>"
                        + "    <specific>192.168.1.1</specific>"
                        + "    <ip-match>10.0.0.*</ip-match>"
                        + "  </definition>"
                        +       "<profiles>"
                                    +"<profile " + "  port=\"1\" " + "  retry=\"2\" "
                                    + "  timeout=\"3\" "
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
                                    + " <label>profile1</label>"
                                    + "<filter>nodeLabel LIKE 'Minion%'</filter>"
                                    + "</profile>"
                                    + "<profile " + "  port=\"18980\" " + "  retry=\"5\" "
                                    + "  timeout=\"300\" "
                                    + "  read-community=\"readCommunity\" "
                                    + "  write-community=\"writeCommunity\" "
                                    + "  proxy-host=\"proxyHost\" "
                                    + "  version=\"v3\" "
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
                                    + " <label>profile2</label>"
                                    + "<filter>nodeLabel LIKE 'Minion%'</filter>"
                                    + "</profile>"
                                + "</profiles>"
                        + "</snmp-config>\n",
                        "target/classes/xsds/snmp-config.xsd" }});
    }
    
    
    /**  Try to validate missing "required" fields and misspellings in "optional" fields **/
    @Test
    public void validateSnmpConfiguration() {
        
        String validConfig =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"public\" "
                + "    write-community=\"private\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        
        try {
            JaxbUtils.unmarshal(SnmpConfig.class, validConfig);
        } catch (Exception e) {
            fail();
        }
        
        String missingFieldConfig =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"public\" "
                + "    wrong-community=\"private\" "
                + "    version=\"v3\">" + "    <range "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        
        try {
            JaxbUtils.unmarshal(SnmpConfig.class, missingFieldConfig);
            fail();
        } catch (Exception e) {
        }
        
        String misspelledConfig =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"public\" "
                + "    wrong-community=\"private\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        
        try {
            JaxbUtils.unmarshal(SnmpConfig.class, misspelledConfig);
            fail();
        } catch (Exception e) {
        }
        
    }

}
