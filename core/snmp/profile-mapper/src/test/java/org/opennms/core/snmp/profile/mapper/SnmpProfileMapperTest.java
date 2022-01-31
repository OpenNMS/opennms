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

import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.config.dao.impl.util.JaxbXmlConverter;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml"
})
@JUnitConfigurationEnvironment
public class SnmpProfileMapperTest {

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    // This tests SnmpPeerFactory w.r.to profiles.
    @Test
    public void testSnmpPeerFactoryWithProfiles() throws IOException {

        URL url = getClass().getResource("/snmp-config.xml");
        assertNotNull(url);
        String configXml = Resources.toString(url, StandardCharsets.UTF_8);
        JaxbXmlConverter converter = new JaxbXmlConverter("snmp-config.xsd", "snmp-config",null);
        String snmpConfigJson = converter.xmlToJson(configXml);
        SnmpConfig config = ConfigConvertUtil.jsonToObject(snmpConfigJson, SnmpConfig.class);
        snmpPeerFactory.updateConfig(config);
        snmpPeerFactory.reload();
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
