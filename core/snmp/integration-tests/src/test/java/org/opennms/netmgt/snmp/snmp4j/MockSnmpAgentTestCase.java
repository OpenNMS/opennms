/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.snmp.snmp4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public abstract class MockSnmpAgentTestCase extends TestCase {
    private InetAddress m_agentAddress;
    private int m_agentPort = 1691;
    private ClassPathResource m_propertiesResource = new ClassPathResource("loadSnmpDataTest.properties");
    
    private MockSnmpAgent m_agent;

    public MockSnmpAgentTestCase() {
        super();

        try {
            m_agentAddress = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            fail(e.toString());
        }
    }

    @Override
    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");

        super.setUp();
        
        MockLogAppender.setupLogging();

        m_agent = MockSnmpAgent.createAgentAndRun(m_propertiesResource, m_agentAddress.getHostAddress() + "/" + m_agentPort);
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
    
        super.tearDown();

        MockUtil.println("------------ End Test " + getName() + " --------------------------");
    }

    protected SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(m_agentAddress);
        config.setPort(m_agentPort);
        config.setVersion(SnmpAgentConfig.VERSION1);
        return config;
    }

    public InetAddress getAgentAddress() {
        return m_agentAddress;
    }

    public void setAgentAddress(InetAddress agentAddress) {
        m_agentAddress = agentAddress;
    }

    public int getAgentPort() {
        return m_agentPort;
    }

    public void setAgentPort(int agentPort) {
        m_agentPort = agentPort;
    }

    public ClassPathResource getPropertiesResource() {
        return m_propertiesResource;
    }

    public void setPropertiesResource(ClassPathResource propertiesResource) {
        m_propertiesResource = propertiesResource;
    }

    public MockSnmpAgent getAgent() {
        return m_agent;
    }
}