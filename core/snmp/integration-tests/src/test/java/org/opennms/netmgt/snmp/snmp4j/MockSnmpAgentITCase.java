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

public abstract class MockSnmpAgentITCase {
    private InetAddress m_agentAddress;
    private int m_agentPort = 1691;
    private ClassPathResource m_propertiesResource = new ClassPathResource("loadSnmpDataTest.properties");
    
    private MockSnmpAgent m_agent;

    public MockSnmpAgentITCase() {
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
			setAgentAddress(InetAddressUtils.getLocalHostAddress());
			MockSnmpStrategy.setDataForAddress(new SnmpAgentAddress(getAgentAddress(), getAgentPort()), m_propertiesResource);
		} else {
			try {
				m_agent = MockSnmpAgent.createAgentAndRun(m_propertiesResource.getURL(), InetAddressUtils.getLocalHostAddress().getHostAddress() + "/0");
			} catch (Throwable e) {
				m_agent = MockSnmpAgent.createAgentAndRun(m_propertiesResource.getURL(), InetAddressUtils.ONE_TWENTY_SEVEN.getHostAddress() + "/0");
			}
			setAgentAddress(m_agent.getInetAddress());
			setAgentPort(m_agent.getPort());
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
