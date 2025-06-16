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
public class JUnitSnmpAgentExecutionListenerIT {
	final SnmpObjId m_oid = SnmpObjId.get(".1.3.5.1.1.1.0");

    @Before
    public void setUp() throws Exception {
    	MockLogAppender.setupLogging();
    	SnmpPeerFactory.setInstance(new ProxySnmpAgentConfigFactory(ConfigurationTestUtils.getInputStreamForConfigFile("snmp-config.xml")));
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:/loadSnmpDataTest.properties", host="192.168.0.254")
    public void testClassAgent() throws Exception {
    	assertEquals(
    			octetString("TestData"),
    			SnmpUtils.get(SnmpPeerFactory.getInstance().getAgentConfig(addr("192.168.0.254")), m_oid)
    	);
    }
    
    @Test
    @JUnitSnmpAgents({
    		@JUnitSnmpAgent(host="192.168.0.1", port=161, resource="classpath:/loadSnmpDataTest.properties"),
    		@JUnitSnmpAgent(host="192.168.0.2", port=161, resource="classpath:/differentSnmpData.properties")
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
