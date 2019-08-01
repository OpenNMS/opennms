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

package org.opennms.core.snmp.profile.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.snmp.profile.mapper.mapper.SnmpProfileMapperImpl;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.filter.api.FilterDao;
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
        assertEquals(profiles.size(), 3);
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
        assertEquals(30000L, agentConfig.getTTL().longValue());
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

        snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig, "Default");
        List<Definition> definitionList = snmpPeerFactory.getSnmpConfig().getDefinitions();
        Optional<Definition> result = definitionList.stream().filter(def -> def.getSpecifics().contains("10.0.1.16")).findFirst();
        assertTrue(result.isPresent());
        Definition definition = result.get();
        assertEquals("power2", definition.getReadCommunity());
        assertEquals("user2", definition.getSecurityName());

    }

    // This tests profile mapper get and save calls.
    @Test
    public void testGetAgentConfigFromMapper() throws InterruptedException {

        URL url = getClass().getResource("/snmp-config.xml");
        assertNotNull(url);
        SnmpPeerFactory.setFile(new File(url.getFile()));
        SnmpPeerFactory snmpPeerFactory = SnmpPeerFactory.getInstance();
        assertNotNull(snmpPeerFactory.getSnmpConfig());
        FilterDao filterDao = Mockito.mock(FilterDao.class);
        SortedMap<Integer, String> nodeMap = new TreeMap<>();
        nodeMap.put(124, "node1");
        nodeMap.put(358, "node2");
        Mockito.when(filterDao.isRuleMatching(Mockito.anyString())).thenReturn(true);
        Mockito.when(filterDao.getNodeMap(Mockito.contains("power"))).thenReturn(nodeMap);
        SnmpProfileMapperImpl  profileMapper = new SnmpProfileMapperImpl();
        profileMapper.setAgentConfigFactory(snmpPeerFactory);
        profileMapper.setFilterDao(filterDao);

        // Should match profile that has no filter-expression and one match
        List<SnmpAgentConfig> agentConfigList = profileMapper.getAgentConfigs(InetAddressUtils
                .getInetAddress("10.0.1.15"));
        assertEquals(2, agentConfigList.size());

        // Should match profile that has no filter-expression and one match
        Mockito.when(filterDao.getNodeMap(Mockito.contains("Minion"))).thenReturn(nodeMap);
        agentConfigList = profileMapper.getAgentConfigs(InetAddressUtils
                .getInetAddress("10.0.1.15"));
        assertEquals(2, agentConfigList.size());
        // No filtering needed, just return the default profile
        Mockito.when(filterDao.getNodeMap(Mockito.anyString())).thenReturn(new TreeMap<>());
        agentConfigList = profileMapper.getAgentConfigs(InetAddressUtils
                .getInetAddress("10.0.1.15"));
        // There is always a default one without filter expression.
        assertEquals(1, agentConfigList.size());
        // Get the default one.
        SnmpAgentConfig agentConfig = agentConfigList.get(0);
        assertEquals(false, agentConfig.isDefault());
        assertEquals(agentConfig.getSecurityName(), "opennmsUser");
        assertEquals(agentConfig.getReadCommunity(), "power3");
        // Definitions shouldn't be updated with the profile config.
        List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
        Optional<Definition> result = definitions.stream().filter(def -> def.getSpecifics().contains("10.0.1.15")).findFirst();
        assertFalse(result.isPresent());

        // Update definition and verify config.
        profileMapper.updateDefinition(agentConfig, "Minion");
        definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
        result = definitions.stream().filter(def -> def.getSpecifics().contains("10.0.1.15")).findFirst();
        assertTrue(result.isPresent());
        Definition definition = result.get();
        System.out.println(definition);
        assertEquals("power3", definition.getReadCommunity());
        assertEquals("Minion", definition.getLocation());

    }

    public void testSnmpProfileWithSnmpWalk() {

    }
}
