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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.snmp.profile.mapper.impl.SnmpProfileMapperImpl;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = "192.0.1.206", resource = "classpath:/snmpProfileTestData.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SnmpProfileMapperIT {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpProfileMapperIT.class);

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;

    private SnmpProfileMapper profileMapper;

    private ProxySnmpAgentConfigFactory snmpPeerFactory;


    @Test(timeout = 30000)
    public void testSnmpProfileMapper() throws UnknownHostException {

        // timeout and ttl from first profile.
        int timeout = 5000;
        long ttl = 10000;
        URL url = getClass().getResource("/snmp-config1.xml");
        try (InputStream configStream = url.openStream()) {
            setUpProfileMapper(configStream, url);
            CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.getAgentConfigFromProfiles(InetAddress.getByName("192.0.1.206"),
                    null);
            Optional<SnmpAgentConfig> agentConfig = future.get();
            assertTrue(agentConfig.isPresent());
            assertEquals(ttl, agentConfig.get().getTTL().longValue());
            assertEquals(timeout, agentConfig.get().getTimeout());
            snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig.get(), "Default", "test");
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertTrue(definition.isPresent());
        } catch (InterruptedException | ExecutionException | IOException e) {
            fail();
        }
    }

    @Test(timeout = 30000)
    public void testSnmpProfileMapperWithCustomOID() {

        // timeout and ttl from profile.
        int timeout = 6000;
        long ttl = 7000;
        URL url = getClass().getResource("/snmp-config2.xml");
        try (InputStream configStream = url.openStream()) {
            setUpProfileMapper(configStream, url);
            CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.getAgentConfigFromProfiles(InetAddress.getByName("192.0.1.206"),
                    "Minion", ".1.3.6.1.2.1.1.3.0");
            Optional<SnmpAgentConfig> agentConfig = future.get();
            assertTrue(agentConfig.isPresent());
            assertEquals(agentConfig.get().getTTL().longValue(), ttl);
            assertEquals(agentConfig.get().getTimeout(), timeout);
            snmpPeerFactory.saveAgentConfigAsDefinition(agentConfig.get(), "Default", "test");
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertTrue(definition.isPresent());
            assertEquals("profile1", definition.get().getProfileLabel());
        } catch (InterruptedException | ExecutionException | IOException e) {
            fail();
        }
    }


    @Test(timeout = 30000)
    public void testFitProfile() throws UnknownHostException {

        // timeout and ttl from profile.
        int timeout = 6000;
        long ttl = 7000;
        SnmpAgentConfig snmpAgentConfig = null;
        URL url = getClass().getResource("/snmp-config3.xml");
        try (InputStream configStream = url.openStream()) {
            setUpProfileMapper(configStream, url);
            //Profile without filter expression should always match and that would be set.
            CompletableFuture<Optional<SnmpAgentConfig>> future = profileMapper.fitProfile("profile1", InetAddress.getByName("192.0.1.206"), "Minion", ".1.3.6.1.2.1.1.3.0");
            Optional<SnmpAgentConfig> agentConfig = future.get();
            if (agentConfig.isPresent()) {
                snmpAgentConfig = agentConfig.get();
            }
            assertNotNull(snmpAgentConfig);
            assertEquals(snmpAgentConfig.getTTL().longValue(), ttl);
            assertEquals(snmpAgentConfig.getTimeout(), timeout);
            // Check that fit profile doesn't actually save definitions in the config.
            List<Definition> definitions = snmpPeerFactory.getSnmpConfig().getDefinitions();
            Optional<Definition> definition = definitions.stream().filter(def -> def.getTimeout() == timeout).findFirst();
            assertFalse(definition.isPresent());
        } catch (InterruptedException | ExecutionException | IOException e) {
            fail();
        }
    }

    private void setUpProfileMapper(InputStream configStream, URL resourceURL) throws FileNotFoundException {
        snmpPeerFactory = new ProxySnmpAgentConfigFactory(configStream);
        // This is to not override snmp-config from etc
        SnmpPeerFactory.setFile(new File(resourceURL.getFile()));
        FilterDao filterDao = Mockito.mock(FilterDao.class);
        when(filterDao.isValid(Mockito.anyString(), Mockito.contains("IPLIKE"))).thenReturn(true);
        profileMapper = new SnmpProfileMapperImpl(filterDao, snmpPeerFactory, locationAwareSnmpClient);
    }

}
