/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.opennms.core.config.impl.JaxbResourceConfiguration;
import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.config.agents.AgentResponse;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.dao.mock.UnimplementedFilterDao;
import org.opennms.netmgt.dao.mock.UnimplementedMonitoredServiceDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.core.io.ClassPathResource;

@RunWith(BlockJUnit4ClassRunner.class)
public class AgentConfigurationResourceTest {
    private TestFilterDao m_filterDao;
    private TestMonitoredServiceDao m_monitoredServiceDao;
    private TestSnmpConfigDao m_snmpConfigDao;
    private AgentConfigurationResource m_configResource;

    @Before
    public void setUp() throws Exception {
        m_filterDao = new TestFilterDao();
        m_monitoredServiceDao = new TestMonitoredServiceDao();
        m_snmpConfigDao = new TestSnmpConfigDao();
        m_configResource = new AgentConfigurationResource();
        m_configResource.setCollectdConfigurationResource(new JaxbResourceConfiguration<CollectdConfiguration>(CollectdConfiguration.class, new ClassPathResource("/collectd-configuration.xml")));
        m_configResource.setFilterDao(m_filterDao);
        m_configResource.setMonitoredServiceDao(m_monitoredServiceDao);
        m_configResource.setAgentConfigFactory(m_snmpConfigDao);
        m_configResource.afterPropertiesSet();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidInputs() throws Exception {
        m_configResource.getAgentsJson(null, null);
    }

    @Test(expected=WebApplicationException.class)
    public void testMissingFilter() throws Exception {
        m_configResource.getAgentsJson("foo", "SNMP");
    }

    @Test
    public void testSimpleFilter() throws Exception {
        final InetAddress oneNinetyTwo = addr("192.168.0.1");

        final List<InetAddress> addresses = Arrays.asList(oneNinetyTwo);
        m_filterDao.setActiveIPAddressList(addresses);

        final OnmsNode node = new OnmsNode(null, "foo");
        node.setId(1);
        node.setForeignSource("foo");
        node.setForeignId("bar");
        node.setSysObjectId(".1.2.3.4.5");
        final OnmsIpInterface iface = new OnmsIpInterface(oneNinetyTwo, node);
        final OnmsServiceType serviceType = new OnmsServiceType("SNMP");
        final OnmsMonitoredService service = new OnmsMonitoredService(iface, serviceType);
        m_monitoredServiceDao.setMatching(Arrays.asList(service));

        final Response response = m_configResource.getAgentsJson("example1", "SNMP");
        assertEquals(200, response.getStatus());
        final Object entity = response.getEntity();
        assertNotNull(entity);
        assertTrue(entity instanceof GenericEntity<?>);
        @SuppressWarnings("unchecked")
        final List<AgentResponse> agentResponses = (List<AgentResponse>) ((GenericEntity<?>)entity).getEntity();
        System.err.println(agentResponses);
        assertEquals(1, agentResponses.size());
        assertEquals(oneNinetyTwo, agentResponses.get(0).getAddress());
        assertEquals(1161, agentResponses.get(0).getPort().intValue());
        assertEquals(".1.2.3.4.5", agentResponses.get(0).getParameters().get("sysObjectId"));
        assertEquals("1", agentResponses.get(0).getParameters().get("nodeId"));
        assertEquals("foo", agentResponses.get(0).getParameters().get("foreignSource"));
        assertEquals("bar", agentResponses.get(0).getParameters().get("foreignId"));
    }
    
    private static final class TestMonitoredServiceDao extends UnimplementedMonitoredServiceDao {
        private List<OnmsMonitoredService> m_services;

        @Override
        public List<OnmsMonitoredService> findMatching(final Criteria criteria) {
            return m_services;
        }

        public void setMatching(final List<OnmsMonitoredService> services) {
            m_services = services;
        }
        
    }

    private static final class TestFilterDao extends UnimplementedFilterDao {
        private List<InetAddress> m_activeAddresses;

        public void setActiveIPAddressList(final List<InetAddress> addresses) {
            m_activeAddresses = addresses;
        }

        @Override
        public List<InetAddress> getActiveIPAddressList(final String rule) throws FilterParseException {
            return m_activeAddresses;
        }
    }
    
    private static final class TestSnmpConfigDao implements SnmpAgentConfigFactory {
        @Override
        public SnmpAgentConfig getAgentConfig(final InetAddress address) {
            return new SnmpAgentConfig(address, getDefaults());
        }

        private static SnmpConfiguration getDefaults() {
            final SnmpConfiguration config = new SnmpConfiguration();
            config.setPort(1161);
            return config;
        }
    }
}
