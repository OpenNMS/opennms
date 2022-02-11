/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.config.convert;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.JaxbXmlConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.snmp.*;

import static org.junit.Assert.*;

public class SnmpConfigTest extends CmConfigTest<SnmpConfig> {

    public SnmpConfigTest(final SnmpConfig sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, "snmp-config.xsd", "snmp-config");
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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

        return Arrays.asList(new Object[][]{
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
                        "target/classes/xsds/snmp-config.xsd"},
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
                        "target/classes/xsds/snmp-config.xsd"},
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
                                + "<profiles>"
                                + "<profile " + "  port=\"1\" " + "  retry=\"2\" "
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
                        "target/classes/xsds/snmp-config.xsd"}});
    }


    /**
     * Try to validate missing "required" fields and misspellings in "optional" fields
     **/
    @Test
    public void validateSnmpConfiguration() throws IOException {

        String validConfig = "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
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

        String missingFieldConfig = "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"public\" "
                + "    wrong-community=\"private\" "
                + "    version=\"v3\">" + "    <range "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";


        try {
            this.validate(missingFieldConfig);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException);
        }

        String misspelledConfig = "<snmp-config " + "  port=\"1\" " + "  retry=\"2\" >"
                + "  <definition "
                + "    read-community=\"public\" "
                + "    wrong-community=\"private\" "
                + "    version=\"v3\">" + "    <range "
                + "      begin=\"192.168.0.1\" "
                + "      end=\"192.168.0.255\"/>"
                + "    <specific>192.168.1.1</specific>"
                + "    <ip-match>10.0.0.*</ip-match>"
                + "  </definition>" + "</snmp-config>\n";

        //wrong-community attribute is wrong should not be present in converted json object
        JaxbXmlConverter converter = new JaxbXmlConverter("snmp-config.xsd", "snmp-config",null);
        String snmpConfigJson = converter.xmlToJson(misspelledConfig);
        JSONObject jsonObject = new JSONObject(snmpConfigJson);
        assertFalse(jsonObject.has("wrong-community"));
    }

    public void validate(String configXml) throws IOException {
        ConfigDefinition def = XsdHelper.buildConfigDefinition("snmp", "snmp-config.xsd",
                "snmp-config", ConfigurationManagerService.BASE_PATH);
        ConfigConverter converter = XsdHelper.getConverter(def);
        String configJson = converter.xmlToJson(configXml);
        def.validate(configJson);
    }

}
