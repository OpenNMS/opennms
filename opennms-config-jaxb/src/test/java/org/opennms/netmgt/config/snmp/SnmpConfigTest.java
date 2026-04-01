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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
        def.setReadCommunity("secretPublic");
        def.setWriteCommunity("secretPrivate");
        def.addRange(range);
        def.addSpecific("192.168.1.1");
        def.addIpMatch("10.0.0.*");
        definitionList.add(def);
        SnmpConfig snmpConfig = new SnmpConfig(
                1, // port
                2, // retry
                3, // timeout
                "secretReadCommunity", "secretWriteCommunity",
                "proxyHost",
                "v2c", // version
                4, // max-vars-per-pdu
                5, // max-repetitions
                484, // max-request-size
                "securityName",
                3, // security-level
                "secretAuthPassphrase",
                "MD5", // auth-protocol
                "engineId", "contextEngineId", "contextName",
                "secretPrivacyPassphrase", "DES", // privacy-protocol
                "enterpriseId", definitionList);
        SnmpProfiles snmpProfiles = new SnmpProfiles();
        SnmpProfile snmpProfile = new SnmpProfile(1, // port
                2, // retry
                3, // timeout
                "secretReadCommunity", "secretWriteCommunity",
                "proxyHost",
                "v2c", // version
                4, // max-vars-per-pdu
                5, // max-repetitions
                484, // max-request-size
                "securityName",
                3, // security-level
                "secretAuthPassphrase",
                "MD5", // auth-protocol
                "engineId", "contextEngineId", "contextName",
                "secretPrivacyPassphrase", "DES", // privacy-protocol
                "enterpriseId",
                "profile1",
                "nodeLabel LIKE 'Minion%'");
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpProfile = new SnmpProfile(18980, // port
                5, // retry
                300, // timeout
                "secretReadCommunity", "secretWriteCommunity",
                "proxyHost",
                "v3", // version
                4, // max-vars-per-pdu
                5, // max-repetitions
                484, // max-request-size
                "securityName",
                3, // security-level
                "secretAuthPassphrase",
                "MD5", // auth-protocol
                "engineId", "contextEngineId", "contextName",
                "secretPrivacyPassphrase", "DES", // privacy-protocol
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
                                   "secretReadCommunity", "secretWriteCommunity",
                                   "proxyHost",
                                   "v2c", // version
                                   4, // max-vars-per-pdu
                                   5, // max-repetitions
                                   484, // max-request-size
                                   "securityName",
                                   3, // security-level
                                   "secretAuthPassphrase",
                                   "MD5", // auth-protocol
                                   "engineId", "contextEngineId", "contextName",
                                   "secretPrivacyPassphrase", "DES", // privacy-protocol
                                   "enterpriseId", definitionList),
                                   "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" "
                                           + "  timeout=\"3\" "
                                           + "  read-community=\"secretReadCommunity\" "
                                           + "  write-community=\"secretWriteCommunity\" "
                                           + "  proxy-host=\"proxyHost\" "
                                           + "  version=\"v2c\" "
                                           + "  max-vars-per-pdu=\"4\" "
                                           + "  max-repetitions=\"5\" "
                                           + "  max-request-size=\"484\" "
                                           + "  security-name=\"securityName\" "
                                           + "  security-level=\"3\" "
                                           + "  auth-passphrase=\"secretAuthPassphrase\" "
                                           + "  auth-protocol=\"MD5\" "
                                           + "  engine-id=\"engineId\" "
                                           + "  context-engine-id=\"contextEngineId\" "
                                           + "  context-name=\"contextName\" "
                                           + "  privacy-passphrase=\"secretPrivacyPassphrase\" "
                                           + "  privacy-protocol=\"DES\" "
                                           + "  enterprise-id=\"enterpriseId\">"
                                           + "  <definition "
                                           + "    read-community=\"secretPublic\" "
                                           + "    write-community=\"secretPrivate\" "
                                           + "    version=\"v3\">" + "    <range "
                                           + "      begin=\"192.168.0.1\" "
                                           + "      end=\"192.168.0.255\"/>"
                                           + "    <specific>192.168.1.1</specific>"
                                           + "    <ip-match>10.0.0.*</ip-match>"
                                           + "  </definition>"
                                           + "</snmp-config>\n",
                    "target/classes/xsds/snmp-config.xsd" },
                {
                    new SnmpConfig(
                                   1, // port
                                   2, // retry
                                   3, // timeout
                                   "secretReadCommunity", "secretWriteCommunity",
                                   "proxyHost",
                                   "v2c", // version
                                   4, // max-vars-per-pdu
                                   5, // max-repetitions
                                   484, // max-request-size
                                   "securityName",
                                   3, // security-level
                                   "secretAuthPassphrase",
                                   "MD5", // auth-protocol
                                   "engineId", "contextEngineId", "contextName",
                                   "secretPrivacyPassphrase", "DES", // privacy-protocol
                                   "enterpriseId", definitionList),
                                   "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" "
                                           + "  port=\"1\" " + "  retry=\"2\" "
                                           + "  timeout=\"3\" "
                                           + "  read-community=\"secretReadCommunity\" "
                                           + "  write-community=\"secretWriteCommunity\" "
                                           + "  proxy-host=\"proxyHost\" "
                                           + "  version=\"v2c\" "
                                           + "  max-vars-per-pdu=\"4\" "
                                           + "  max-repetitions=\"5\" "
                                           + "  max-request-size=\"484\" "
                                           + "  security-name=\"securityName\" "
                                           + "  security-level=\"3\" "
                                           + "  auth-passphrase=\"secretAuthPassphrase\" "
                                           + "  auth-protocol=\"MD5\" "
                                           + "  engine-id=\"engineId\" "
                                           + "  context-engine-id=\"contextEngineId\" "
                                           + "  context-name=\"contextName\" "
                                           + "  privacy-passphrase=\"secretPrivacyPassphrase\" "
                                           + "  privacy-protocol=\"DES\" "
                                           + "  enterprise-id=\"enterpriseId\">"
                                           + "  <definition "
                                           + "    read-community=\"secretPublic\" "
                                           + "    write-community=\"secretPrivate\" "
                                           + "    version=\"v3\">" + "    <range "
                                           + "      begin=\"192.168.0.1\" "
                                           + "      end=\"192.168.0.255\"/>"
                                           + "    <specific>192.168.1.1</specific>"
                                           + "    <ip-match>10.0.0.*</ip-match>"
                                           + "  </definition>"
                                           + "</snmp-config>\n",
                    "target/classes/xsds/snmp-config.xsd" },
                {
                    snmpConfig,
                        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" "
                        + "  port=\"1\" " + "  retry=\"2\" "
                        + "  timeout=\"3\" "
                        + "  read-community=\"secretReadCommunity\" "
                        + "  write-community=\"secretWriteCommunity\" "
                        + "  proxy-host=\"proxyHost\" "
                        + "  version=\"v2c\" "
                        + "  max-vars-per-pdu=\"4\" "
                        + "  max-repetitions=\"5\" "
                        + "  max-request-size=\"484\" "
                        + "  security-name=\"securityName\" "
                        + "  security-level=\"3\" "
                        + "  auth-passphrase=\"secretAuthPassphrase\" "
                        + "  auth-protocol=\"MD5\" "
                        + "  engine-id=\"engineId\" "
                        + "  context-engine-id=\"contextEngineId\" "
                        + "  context-name=\"contextName\" "
                        + "  privacy-passphrase=\"secretPrivacyPassphrase\" "
                        + "  privacy-protocol=\"DES\" "
                        + "  enterprise-id=\"enterpriseId\">"
                        + "  <definition "
                        + "    read-community=\"secretPublic\" "
                        + "    write-community=\"secretPrivate\" "
                        + "    version=\"v3\">" + "    <range "
                        + "      begin=\"192.168.0.1\" "
                        + "      end=\"192.168.0.255\"/>"
                        + "    <specific>192.168.1.1</specific>"
                        + "    <ip-match>10.0.0.*</ip-match>"
                        + "  </definition>"
                        +       "<profiles>"
                                    +"<profile " + "  port=\"1\" " + "  retry=\"2\" "
                                    + "  timeout=\"3\" "
                                    + "  read-community=\"secretReadCommunity\" "
                                    + "  write-community=\"secretWriteCommunity\" "
                                    + "  proxy-host=\"proxyHost\" "
                                    + "  version=\"v2c\" "
                                    + "  max-vars-per-pdu=\"4\" "
                                    + "  max-repetitions=\"5\" "
                                    + "  max-request-size=\"484\" "
                                    + "  security-name=\"securityName\" "
                                    + "  security-level=\"3\" "
                                    + "  auth-passphrase=\"secretAuthPassphrase\" "
                                    + "  auth-protocol=\"MD5\" "
                                    + "  engine-id=\"engineId\" "
                                    + "  context-engine-id=\"contextEngineId\" "
                                    + "  context-name=\"contextName\" "
                                    + "  privacy-passphrase=\"secretPrivacyPassphrase\" "
                                    + "  privacy-protocol=\"DES\" "
                                    + "  enterprise-id=\"enterpriseId\">"
                                    + " <label>profile1</label>"
                                    + "<filter>nodeLabel LIKE 'Minion%'</filter>"
                                    + "</profile>"
                                    + "<profile " + "  port=\"18980\" " + "  retry=\"5\" "
                                    + "  timeout=\"300\" "
                                    + "  read-community=\"secretReadCommunity\" "
                                    + "  write-community=\"secretWriteCommunity\" "
                                    + "  proxy-host=\"proxyHost\" "
                                    + "  version=\"v3\" "
                                    + "  max-vars-per-pdu=\"4\" "
                                    + "  max-repetitions=\"5\" "
                                    + "  max-request-size=\"484\" "
                                    + "  security-name=\"securityName\" "
                                    + "  security-level=\"3\" "
                                    + "  auth-passphrase=\"secretAuthPassphrase\" "
                                    + "  auth-protocol=\"MD5\" "
                                    + "  engine-id=\"engineId\" "
                                    + "  context-engine-id=\"contextEngineId\" "
                                    + "  context-name=\"contextName\" "
                                    + "  privacy-passphrase=\"secretPrivacyPassphrase\" "
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
                + "    read-community=\"secretPublic\" "
                + "    write-community=\"secretPrivate\" "
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
                + "    read-community=\"secretPublic\" "
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
                + "    read-community=\"secretPublic\" "
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

    @Test
    public void verifyToStringMasksPasswords() {
        final String authPassphrase = "myAuthSecret";
        final String privacyPassphrase = "myPrivSecret";
        final String readCommunity = "secretRead";
        final String writeCommunity = "secretWrite";
        final String maskedPassword = "******";

        // Configuration
        final Configuration configuration = new Configuration();
        configuration.setAuthPassphrase(authPassphrase);
        configuration.setPrivacyPassphrase(privacyPassphrase);
        configuration.setReadCommunity(readCommunity);
        configuration.setWriteCommunity(writeCommunity);
        String configStr = configuration.toString();
        assertFalse("Configuration.toString() should not contain authPassphrase", configStr.contains(authPassphrase));
        assertFalse("Configuration.toString() should not contain privacyPassphrase", configStr.contains(privacyPassphrase));
        assertFalse("Configuration.toString() should not contain readCommunity", configStr.contains(readCommunity));
        assertFalse("Configuration.toString() should not contain writeCommunity", configStr.contains(writeCommunity));
        assertTrue("Configuration.toString() should contain masked password", configStr.contains("authPassphrase=" + maskedPassword));
        assertTrue("Configuration.toString() should contain masked password", configStr.contains("privacyPassphrase=" + maskedPassword));
        assertTrue("Configuration.toString() should contain masked password", configStr.contains("readCommunity=" + maskedPassword));
        assertTrue("Configuration.toString() should contain masked password", configStr.contains("writeCommunity=" + maskedPassword));

        // Definition
        final Definition definition = new Definition();
        definition.setAuthPassphrase(authPassphrase);
        definition.setPrivacyPassphrase(privacyPassphrase);
        definition.setReadCommunity(readCommunity);
        definition.setWriteCommunity(writeCommunity);
        String defStr = definition.toString();
        assertFalse("Definition.toString() should not contain authPassphrase", defStr.contains(authPassphrase));
        assertFalse("Definition.toString() should not contain privacyPassphrase", defStr.contains(privacyPassphrase));
        assertFalse("Definition.toString() should not contain readCommunity", defStr.contains(readCommunity));
        assertFalse("Definition.toString() should not contain writeCommunity", defStr.contains(writeCommunity));
        assertTrue("Definition.toString() should contain masked password", defStr.contains("authPassphrase=" + maskedPassword));
        assertTrue("Definition.toString() should contain masked password", defStr.contains("privacyPassphrase=" + maskedPassword));
        assertTrue("Definition.toString() should contain masked password", defStr.contains("readCommunity=" + maskedPassword));
        assertTrue("Definition.toString() should contain masked password", defStr.contains("writeCommunity=" + maskedPassword));

        // SnmpConfig
        final SnmpConfig snmpCfg = new SnmpConfig();
        snmpCfg.setAuthPassphrase(authPassphrase);
        snmpCfg.setPrivacyPassphrase(privacyPassphrase);
        snmpCfg.setReadCommunity(readCommunity);
        snmpCfg.setWriteCommunity(writeCommunity);
        String snmpCfgStr = snmpCfg.toString();
        assertFalse("SnmpConfig.toString() should not contain authPassphrase", snmpCfgStr.contains(authPassphrase));
        assertFalse("SnmpConfig.toString() should not contain privacyPassphrase", snmpCfgStr.contains(privacyPassphrase));
        assertFalse("SnmpConfig.toString() should not contain readCommunity", snmpCfgStr.contains(readCommunity));
        assertFalse("SnmpConfig.toString() should not contain writeCommunity", snmpCfgStr.contains(writeCommunity));
        assertTrue("SnmpConfig.toString() should contain masked password", snmpCfgStr.contains("authPassphrase=" + maskedPassword));
        assertTrue("SnmpConfig.toString() should contain masked password", snmpCfgStr.contains("privacyPassphrase=" + maskedPassword));
        assertTrue("SnmpConfig.toString() should contain masked password", snmpCfgStr.contains("readCommunity=" + maskedPassword));
        assertTrue("SnmpConfig.toString() should contain masked password", snmpCfgStr.contains("writeCommunity=" + maskedPassword));

        // SnmpProfile
        final SnmpProfile profile = new SnmpProfile();
        profile.setAuthPassphrase(authPassphrase);
        profile.setPrivacyPassphrase(privacyPassphrase);
        profile.setReadCommunity(readCommunity);
        profile.setWriteCommunity(writeCommunity);
        String profileStr = profile.toString();
        assertFalse("SnmpProfile.toString() should not contain authPassphrase", profileStr.contains(authPassphrase));
        assertFalse("SnmpProfile.toString() should not contain privacyPassphrase", profileStr.contains(privacyPassphrase));
        assertFalse("SnmpProfile.toString() should not contain readCommunity", profileStr.contains(readCommunity));
        assertFalse("SnmpProfile.toString() should not contain writeCommunity", profileStr.contains(writeCommunity));
        assertTrue("SnmpProfile.toString() should contain masked password", profileStr.contains("authPassphrase=" + maskedPassword));
        assertTrue("SnmpProfile.toString() should contain masked password", profileStr.contains("privacyPassphrase=" + maskedPassword));
        assertTrue("SnmpProfile.toString() should contain masked password", profileStr.contains("readCommunity=" + maskedPassword));
        assertTrue("SnmpProfile.toString() should contain masked password", profileStr.contains("writeCommunity=" + maskedPassword));
    }

    @Test
    public void verifyDataSnmpConfigToStringMasksPasswords() throws ParseException {
        final String maskedPassword = "******";
        // Secret values used in data() for the SnmpConfig top-level config
        final List<String> configSecrets = Arrays.asList("secretAuthPassphrase", "secretPrivacyPassphrase", "secretReadCommunity", "secretWriteCommunity");
        // Secret values used in data() for nested Definitions
        final List<String> definitionSecrets = Arrays.asList("secretPublic", "secretPrivate");

        for (final Object[] params : data()) {
            final SnmpConfig snmpConfig = (SnmpConfig) params[0];
            final String str = snmpConfig.toString();

            for (final String secret : configSecrets) {
                assertFalse("SnmpConfig.toString() should not contain '" + secret + "': " + str,
                        str.contains(secret));
            }
            assertTrue("SnmpConfig.toString() should contain masked authPassphrase",
                    str.contains("authPassphrase=" + maskedPassword));
            assertTrue("SnmpConfig.toString() should contain masked privacyPassphrase",
                    str.contains("privacyPassphrase=" + maskedPassword));
            assertTrue("SnmpConfig.toString() should contain masked readCommunity",
                    str.contains("readCommunity=" + maskedPassword));
            assertTrue("SnmpConfig.toString() should contain masked writeCommunity",
                    str.contains("writeCommunity=" + maskedPassword));

            // Also verify nested Definitions mask their secrets
            for (final Definition def : snmpConfig.getDefinitions()) {
                final String defStr = def.toString();
                for (final String secret : definitionSecrets) {
                    assertFalse("Definition.toString() should not contain '" + secret + "': " + defStr,
                            defStr.contains(secret));
                }
                assertTrue("Definition.toString() should contain masked authPassphrase",
                        defStr.contains("authPassphrase=" + maskedPassword));
                assertTrue("Definition.toString() should contain masked privacyPassphrase",
                        defStr.contains("privacyPassphrase=" + maskedPassword));
                assertTrue("Definition.toString() should contain masked readCommunity",
                        defStr.contains("readCommunity=" + maskedPassword));
                assertTrue("Definition.toString() should contain masked writeCommunity",
                        defStr.contains("writeCommunity=" + maskedPassword));
            }

            // Verify nested SnmpProfiles mask their secrets if present
            if (snmpConfig.getSnmpProfiles() != null) {
                for (final SnmpProfile profile : snmpConfig.getSnmpProfiles().getSnmpProfiles()) {
                    final String profileStr = profile.toString();
                    for (final String secret : configSecrets) {
                        assertFalse("SnmpProfile.toString() should not contain '" + secret + "': " + profileStr,
                                profileStr.contains(secret));
                    }
                    assertTrue("SnmpProfile.toString() should contain masked authPassphrase",
                            profileStr.contains("authPassphrase=" + maskedPassword));
                    assertTrue("SnmpProfile.toString() should contain masked privacyPassphrase",
                            profileStr.contains("privacyPassphrase=" + maskedPassword));
                    assertTrue("SnmpProfile.toString() should contain masked readCommunity",
                            profileStr.contains("readCommunity=" + maskedPassword));
                    assertTrue("SnmpProfile.toString() should contain masked writeCommunity",
                            profileStr.contains("writeCommunity=" + maskedPassword));
                }
            }
        }
    }

    @Test
    public void validateSnmpConfigurationWithSecurityLevel() {
        SnmpConfig config = null;

        // securinks. tyLevel can be null
        String validConfigWithNull =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"secretPublic\" "
                + "    write-community=\"secretPrivate\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        try {
            config = JaxbUtils.unmarshal(SnmpConfig.class, validConfigWithNull);
        } catch (Exception e) {
            fail();
        }

        assertFalse(config.hasSecurityLevel());
        assertNull(config.getSecurityLevel());

        // try setting securityLevel to 0, it should result in 1
        config.setSecurityLevel(0);
        assertTrue(config.hasSecurityLevel());
        assertNotNull(config.getSecurityLevel());
        assertEquals(1, config.getSecurityLevel().intValue());

        config = null;

        // securityLevel cannot be 0
        String invalidConfig =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    security-level=\"0\" "
                + "    read-community=\"secretPublic\" "
                + "    write-community=\"secretPrivate\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        try {
            JaxbUtils.unmarshal(SnmpConfig.class, invalidConfig);
            fail();
        } catch (Exception e) {
        }

        // securityLevel can be between 1-3
        String validConfigWithSecurityLevel =  "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    security-level=\"1\" "
                + "    read-community=\"secretPublic\" "
                + "    write-community=\"secretPrivate\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";
        try {
            config = JaxbUtils.unmarshal(SnmpConfig.class, validConfigWithSecurityLevel);
        } catch (Exception e) {
            fail();
        }

        final var def0 = config.getDefinitions().get(0);
        assertNotNull(def0);
        assertTrue(def0.hasSecurityLevel());

        Integer intObject = def0.getSecurityLevel();
        assertNotNull(intObject);
        assertEquals(1, intObject.intValue());
    }
}
