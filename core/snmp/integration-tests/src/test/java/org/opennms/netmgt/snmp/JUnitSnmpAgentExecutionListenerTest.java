/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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



package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author brozow
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
public class JUnitSnmpAgentExecutionListenerTest {
	final SnmpObjId m_oid = SnmpObjId.get(".1.3.5.1.1.1.0");

    @Before
    public void setUp() throws Exception {
    	MockLogAppender.setupLogging();
    	SnmpPeerFactory.setInstance(new ProxySnmpAgentConfigFactory(ConfigurationTestUtils.getInputStreamForConfigFile("snmp-config.xml")));
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:loadSnmpDataTest.properties", host="192.168.0.254")
    public void testClassAgent() throws Exception {
    	assertEquals(
    			octetString("TestData"),
    			SnmpUtils.get(SnmpPeerFactory.getInstance().getAgentConfig(addr("192.168.0.254")), m_oid)
    	);
    }
    
    @Test
    @JUnitSnmpAgents({
    		@JUnitSnmpAgent(host="192.168.0.1", port=161, resource="classpath:loadSnmpDataTest.properties"),
    		@JUnitSnmpAgent(host="192.168.0.2", port=161, resource="classpath:differentSnmpData.properties")
    })
    public void testMultipleHosts() throws Exception {
    	assertEquals(
    			octetString("TestData"),
    			SnmpUtils.get(SnmpPeerFactory.getInstance().getAgentConfig(addr("192.168.0.1")), m_oid)
    	);
    	assertEquals(
    			octetString("DifferentTestData"),
    			SnmpUtils.get(SnmpPeerFactory.getInstance().getAgentConfig(addr("192.168.0.2")), m_oid)
    	);
    }
    
    private SnmpValue octetString(String s) {
    	return SnmpUtils.getValueFactory().getOctetString(s.getBytes());
    }
}
