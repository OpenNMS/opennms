/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitSnmpAgent(port=TcaDataIT.TEST_SNMP_PORT, host=TcaDataIT.TEST_IP_ADDRESS, resource="classpath:/juniperTcaSample.properties")
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
