/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.config.SnmpPeerFactory.ENABLE_ENCRYPTION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-encrypt-util.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class)
public class SnmpEncrypionIT {

    static {
        System.setProperty(ENABLE_ENCRYPTION, "true");
    }

    private SnmpPeerFactory snmpPeerFactory;

    @Test
    public void testEncryption() throws IOException {
        URL url = getClass().getResource("/snmp-config.xml");
        try (InputStream configStream = url.openStream()) {
            snmpPeerFactory = new SnmpPeerFactory(new InputStreamResource(configStream));

            SnmpPeerFactory.setFile(new File(url.getFile()));
            // Check if encryption is enabled
            assertTrue(snmpPeerFactory.getEncryptionEnabled());
            // Check that it is loaded from test resource.
            assertEquals(snmpPeerFactory.getSnmpConfig().getReadCommunity(), "minion");
            assertTrue(snmpPeerFactory.getSnmpConfig().getDefinitions().isEmpty());
            Definition definition = new Definition();
            definition.setSpecifics(Arrays.asList("127.0.0.1"));
            definition.setReadCommunity("Sentinel");
            definition.setWriteCommunity("Alec");
            definition.setAuthPassphrase("OpenNMS");
            definition.setPrivacyPassphrase("Minion");
            snmpPeerFactory.saveDefinition(definition);
            snmpPeerFactory.saveCurrent();
            String configAsString = snmpPeerFactory.getSnmpConfigAsString();
            SnmpConfig config = JaxbUtils.unmarshal(SnmpConfig.class, configAsString);
            // Verify that marshalled strings are different from actual
            assertEquals(config.getDefinitions().size(), 1);
            config.getDefinitions().forEach(def -> {
                assertNotEquals(def.getAuthPassphrase(), "OpenNMS");
                assertNotEquals(def.getReadCommunity(), "Sentinel");
                assertNotEquals(def.getWriteCommunity(), "Alec");
            });
            // Getting config should always resolve to actual strings.
            config = snmpPeerFactory.getSnmpConfig();
            config.getDefinitions().forEach(def -> {
                assertEquals("OpenNMS", def.getAuthPassphrase());
                assertEquals("Sentinel", def.getReadCommunity());
                assertEquals("Alec", def.getWriteCommunity());
            });
            SnmpAgentConfig agentConfig = snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress("127.0.0.1"));
            assertEquals("OpenNMS", agentConfig.getAuthPassPhrase());
            assertEquals("Minion", agentConfig.getPrivPassPhrase());

            // Test saving from SnmpEventInfo.
            SnmpEventInfo info = new SnmpEventInfo();
            info.setVersion("v1");
            info.setReadCommunityString("snmp1");
            info.setWriteCommunityString("snmp2");
            info.setFirstIPAddress("127.0.0.2");
            info.setLastIPAddress("127.0.0.9");
            snmpPeerFactory.define(info);
            snmpPeerFactory.saveCurrent();
            agentConfig = snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress("127.0.0.5"));
            assertEquals("snmp1", agentConfig.getReadCommunity());
            assertEquals("snmp2", agentConfig.getWriteCommunity());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
