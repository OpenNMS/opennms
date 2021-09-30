/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SnmpProfilesTest  extends XmlTestNoCastor<SnmpProfiles> {

    public SnmpProfilesTest(SnmpProfiles sampleObject, String sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
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
                "*.opennms.com");
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
                "privacyPassphrase", "DES",// privacy-protocol
                "enterpriseId", //label
                "profile2", "nodeLabel LIKE 'Minion%'");
        snmpProfiles.addSnmpProfile(snmpProfile);
        return Arrays.asList(new Object[][]{{
                snmpProfiles,
                        "<profiles>"
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
                                + "<filter>*.opennms.com</filter>"
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
                        + "</profiles>\n",
                "target/classes/xsds/snmp-config.xsd"}});
    }
}
