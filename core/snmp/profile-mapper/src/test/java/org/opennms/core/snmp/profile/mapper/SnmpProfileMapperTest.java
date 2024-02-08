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
package org.opennms.core.snmp.profile.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class SnmpProfileMapperTest {

    // This tests SnmpPeerFactory w.r.to profiles.
    @Test
    public void testSnmpPeerFactoryWithProfiles() {

        URL url = getClass().getResource("/snmp-config.xml");
        assertNotNull(url);
        SnmpPeerFactory.setFile(new File(url.getFile()));
        SnmpPeerFactory snmpPeerFactory = SnmpPeerFactory.getInstance();
        assertNotNull(snmpPeerFactory.getSnmpConfig());
        List<SnmpProfile> profiles = snmpPeerFactory.getProfiles();
        assertEquals(4, profiles.size());
        SnmpAgentConfig agentConfig = snmpPeerFactory.getAgentConfigFromProfile(profiles.get(0),
                InetAddressUtils.getInetAddress("10.0.1.12"));
        // Check that it picks up config from profile.
        assertEquals("v3", agentConfig.getVersionAsString());
        assertEquals("0p3nNMSv3", agentConfig.getAuthPassPhrase());
        assertEquals("power1", agentConfig.getReadCommunity());
        assertEquals("user1", agentConfig.getSecurityName());
        // Check that it picks up config from default config
        assertEquals(161, agentConfig.getPort());
        assertEquals(3, agentConfig.getRetries());
        assertEquals(3000, agentConfig.getTimeout());
        assertEquals(3000L, agentConfig.getTTL().longValue());
        assertEquals("private", agentConfig.getWriteCommunity());
        // Check that it picks up config from defaults.
        assertEquals("MD5", agentConfig.getAuthProtocol());
        //Check that ipAddress maches
        assertEquals("10.0.1.12", agentConfig.getAddress().getHostAddress());
        agentConfig = snmpPeerFactory.getAgentConfigFromProfile(profiles.get(1),
                InetAddressUtils.getInetAddress("10.0.1.16"));
        //Check defaults
        assertEquals("DES", agentConfig.getPrivProtocol());
        assertEquals(2, agentConfig.getMaxRepetitions());
        assertEquals(10, agentConfig.getMaxVarsPerPdu());
        assertEquals(65535, agentConfig.getMaxRequestSize());

        snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig, "Default", "test");
        List<Definition> definitionList = snmpPeerFactory.getSnmpConfig().getDefinitions();
        Optional<Definition> result = definitionList.stream().filter(def -> def.getSpecifics().contains("10.0.1.16")).findFirst();
        assertTrue(result.isPresent());
        Definition definition = result.get();
        assertEquals("power2", definition.getReadCommunity());
        assertEquals("user2", definition.getSecurityName());

    }

}
