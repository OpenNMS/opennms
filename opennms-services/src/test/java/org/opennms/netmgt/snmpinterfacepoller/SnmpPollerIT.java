/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.config.SnmpInterfacePollerConfigFactory;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-snmpPollerTest.xml"
})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class SnmpPollerIT implements TemporaryDatabaseAware<MockDatabase> {

    private MockDatabase m_database;

    private MockNetwork m_network;

    private SnmpInterfacePollerConfig m_pollerConfig;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private PollableNetwork m_pollableNetwork;

    private SnmpPoller m_poller;

    //
    // SetUp and TearDown
    //
    @Before
    public void setUp() throws Exception {

        m_network = new MockNetwork();

        m_network.addNode(1, "node1");
        m_network.addInterface(1, "192.168.1.1")
                        .setIfType(6);
        m_network.addInterface(1, "192.168.1.2")
                        .setIfType(2);
        m_database.populate(m_network);

        InputStream configFile = m_resourceLoader.getResource("classpath:/etc/snmp-interface-poller-configuration.xml").getInputStream();
        m_pollerConfig = new SnmpInterfacePollerConfigFactory(1L, configFile);

        m_poller = new SnmpPoller();
        m_poller.setNetwork(m_pollableNetwork);
        m_poller.setPollerConfig(m_pollerConfig);
        m_poller.onInit();

    }

    @Test
    public void testIgnoredInterfaces() throws Exception {
        SnmpInterfaceDao snmpInterfaceDao = ((DefaultPollContext)m_pollableNetwork.getContext()).getSnmpInterfaceDao();

        var interfaces = snmpInterfaceDao.findAll();

        OnmsSnmpInterface ethIf = interfaces.stream().filter(snmpIf -> snmpIf.getIfType() == 6).findFirst().orElseThrow();
        OnmsSnmpInterface otherIf = interfaces.stream().filter(snmpIf -> snmpIf.getIfType() == 2).findFirst().orElseThrow();

        assertEquals(ethIf.getPoll(), "P");
        assertEquals(otherIf.getPoll(), "I");
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }
}

