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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.snmp.profile.mapper.impl.SnmpProfileMapperImpl;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.config.snmp.SnmpProfiles;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = "192.0.1.206", resource = "classpath:/snmpTestData1.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SnmpProfileMapperIT {

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;

    private FilterDao filterDao;

    private SnmpProfileMapperImpl profileMapper;


    @Before
    public void setup() throws IOException {
        SnmpPeerFactory.setInstance(snmpPeerFactory);
        assertTrue(snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
        filterDao = Mockito.mock(FilterDao.class);
        when(filterDao.isValid(Mockito.anyString(), Mockito.contains("IPLIKE"))).thenReturn(true);
        profileMapper = new SnmpProfileMapperImpl(filterDao, snmpPeerFactory, locationAwareSnmpClient);
    }

    @Test
    public void testSnmpProfileMapper() throws UnknownHostException {

        int timeout = 4000;
        long ttl = 6000;
        SnmpProfiles snmpProfiles = new SnmpProfiles();
        SnmpProfile snmpProfile = new SnmpProfile();
        snmpProfile = new SnmpProfile();
        snmpProfile.setLabel("profile1");
        snmpProfile.setVersion("v1");
        snmpProfile.setTimeout(timeout);
        snmpProfile.setTTL(ttl);
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpProfile = new SnmpProfile();
        snmpProfile.setLabel("profile2");
        snmpProfile.setVersion("v1");
        snmpProfile.setFilterExpression("IPADDR IPLIKE 192.0.*.*");
        snmpProfile.setTimeout(10000);
        snmpProfile.setTTL(ttl);
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpProfile = new SnmpProfile();
        snmpProfile.setLabel("profile3");
        snmpProfile.setVersion("v2c");
        snmpProfile.setTimeout(12000);
        snmpProfile.setTTL(ttl);
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpPeerFactory.getSnmpConfig().setSnmpProfiles(snmpProfiles);
        CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.getAgentConfigFromProfiles(InetAddress.getByName("192.0.1.206"),
                null);
        try {
            Optional<SnmpAgentConfig> agentConfig = future.get();
            assertTrue(agentConfig.isPresent());
            assertEquals(ttl, agentConfig.get().getTTL().longValue());
            assertEquals(timeout, agentConfig.get().getTimeout());
            snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig.get(), "Default", "test");
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertTrue(definition.isPresent());
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void testSnmpProfileMapperWithCustomOID() throws UnknownHostException {

        // Add profile with custom timeout and ttl
        int timeout = 6000;
        long ttl = 7000;
        SnmpProfiles snmpProfiles = new SnmpProfiles();
        SnmpProfile snmpProfile = new SnmpProfile();
        snmpProfile.setLabel("profile1");
        snmpProfile.setVersion("v1");
        snmpProfile.setTimeout(timeout);
        snmpProfile.setTTL(ttl);
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpPeerFactory.getSnmpConfig().setSnmpProfiles(snmpProfiles);
        //Profile without filter expression should always match and that would be set.
        CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.getAgentConfigFromProfiles(InetAddress.getByName("192.0.1.206"),
                "Minion", ".1.3.6.1.2.1.1.3.0");
        try {
            Optional<SnmpAgentConfig> agentConfig = future.get();
            assertTrue(agentConfig.isPresent());
            assertEquals(agentConfig.get().getTTL().longValue(), ttl);
            assertEquals(agentConfig.get().getTimeout(), timeout);
            snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig.get(), "Default", "test");
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertTrue(definition.isPresent());
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }


    @Test
    public void testFitProfile() throws UnknownHostException {

        // Add profile with custom timeout and ttl
        int timeout = 10000;
        long ttl = 8000;
        SnmpAgentConfig snmpAgentConfig = null;
        SnmpProfiles snmpProfiles = new SnmpProfiles();
        SnmpProfile snmpProfile = new SnmpProfile();
        snmpProfile.setLabel("profile1");
        snmpProfile.setVersion("v1");
        snmpProfile.setTimeout(timeout);
        snmpProfile.setTTL(ttl);
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpPeerFactory.getSnmpConfig().setSnmpProfiles(snmpProfiles);
        //Profile without filter expression should always match and that would be set.
        CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.fitProfile("profile1", InetAddress.getByName("192.0.1.206"), "Minion", ".1.3.6.1.2.1.1.3.0");
        try {
            Optional<SnmpAgentConfig> agentConfig = future.get();
            if (agentConfig.isPresent()) {
                snmpAgentConfig = agentConfig.get();
            }
            assertNotNull(snmpAgentConfig);
            assertEquals(snmpAgentConfig.getTTL().longValue(), ttl);
            assertEquals(snmpAgentConfig.getTimeout(), timeout);
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertFalse(definition.isPresent());
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }

    }


}
