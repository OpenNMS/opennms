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
package org.opennms.netmgt.collectd.tca;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * The Class TcaDataTest.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitSnmpAgent(port=TcaDataIT.TEST_SNMP_PORT, host=TcaDataIT.TEST_IP_ADDRESS, resource="classpath:/juniperTcaSample.properties")
@JUnitConfigurationEnvironment
public class TcaDataIT implements InitializingBean {

	static final int TEST_SNMP_PORT = 9161;
	static final String TEST_IP_ADDRESS = "127.0.0.1";


	/** The SNMP peer factory. */
	@Autowired
	private SnmpPeerFactory m_snmpPeerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();
		SnmpPeerFactory.setInstance(m_snmpPeerFactory);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
		MockLogAppender.assertNoWarningsOrGreater();
	}

	/**
	 * Test tracker.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testTracker() throws Exception {
		InetAddress localhost = InetAddressUtils.getInetAddress(TEST_IP_ADDRESS);
		SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(localhost);
		TcaData data = new TcaData(localhost);
		try(SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "TcaCollector for " + localhost, data)) {
	        walker.start();
	        walker.waitFor();
	        Assert.assertFalse(walker.failed());
		}
		Assert.assertFalse(data.isEmpty());
		Assert.assertEquals(2, data.getEntries().size());
	}

}
