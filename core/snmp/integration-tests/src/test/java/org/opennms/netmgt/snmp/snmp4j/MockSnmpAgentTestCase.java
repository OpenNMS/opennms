/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;

public abstract class MockSnmpAgentTestCase {
    private InetAddress m_agentAddress;
    private int m_agentPort = 1691;
    private ClassPathResource m_propertiesResource = new ClassPathResource("loadSnmpDataTest.properties");
    
    private MockSnmpAgent m_agent;

    public MockSnmpAgentTestCase() {
        setAgentAddress(InetAddressUtils.getLocalHostAddress());
    }

    @Before
    public void setUp() throws Exception {
        MockUtil.println("------------ Strategy = " + System.getProperty("org.opennms.snmp.strategyClass")+" --------------------------");

        MockLogAppender.setupLogging();

        agentSetup();
    }

	protected void agentSetup() throws InterruptedException, IOException {
		if (usingMockStrategy()) {
			MockSnmpStrategy.setDataForAddress(new SnmpAgentAddress(getAgentAddress(), getAgentPort()), m_propertiesResource);
		} else {
			m_agent = MockSnmpAgent.createAgentAndRun(m_propertiesResource.getURL(), getAgentAddress().getHostAddress() + "/" + m_agentPort);
		}
	}

	protected boolean usingMockStrategy() {
		return MockSnmpStrategy.class.getName().equals(System.getProperty("org.opennms.snmp.strategyClass"));
	}

	@After
    public void tearDown() throws Exception {

        agentCleanup();
    
        //MockLogAppender.assertNoWarningsOrGreater();

        MockUtil.println("------------ End Test --------------------------");
    }

	protected void agentCleanup() throws InterruptedException {
		MockSnmpStrategy.removeHost(new SnmpAgentAddress(getAgentAddress(), getAgentPort()));

		if (m_agent != null) {
			m_agent.shutDownAndWait();
		}
		
	}

    protected SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(getAgentAddress());
        config.setPort(getAgentPort());
        config.setVersion(SnmpAgentConfig.VERSION1);
        return config;
    }

    public InetAddress getAgentAddress() {
        return m_agentAddress;
    }

    protected void setAgentAddress(final InetAddress agentAddress) {
        m_agentAddress = agentAddress;
    }

    public int getAgentPort() {
        return m_agentPort;
    }

    protected void setAgentPort(final int agentPort) {
        m_agentPort = agentPort;
    }

    public ClassPathResource getPropertiesResource() {
        return m_propertiesResource;
    }

    public void setPropertiesResource(final ClassPathResource propertiesResource) {
        m_propertiesResource = propertiesResource;
    }

    public MockSnmpAgent getAgent() {
        return m_agent;
    }
}
