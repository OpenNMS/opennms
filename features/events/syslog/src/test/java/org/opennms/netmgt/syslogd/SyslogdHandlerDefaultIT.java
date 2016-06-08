/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Component;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.KeyValueHolder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogdHandlerDefaultIT extends CamelBlueprintTest {

	@ClassRule
	public static ActiveMQBroker s_broker = new ActiveMQBroker();

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Create a mock SyslogdConfig
		SyslogConfigBean config = new SyslogConfigBean();
		config.setSyslogPort(10514);
		config.setNewSuspectOnMessage(false);
		config.setParser("org.opennms.netmgt.syslogd.CustomSyslogParser");
		config.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
		config.setMatchingGroupHost(4);
		config.setMatchingGroupMessage(7);
		config.setDiscardUei("DISCARD-MATCHING-MESSAGES");

		services.put(SyslogdConfig.class.getName(), new KeyValueHolder<Object, Dictionary>(config, new Properties()));
		Properties props = new Properties();
		props.setProperty("alias", "onms.broker");
		services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(
				ActiveMQComponent.activeMQComponent("vm://localhost?create=false"), props));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-syslog-handler-default.xml";
	}

	@Test(timeout=60000)
	public void testSyslogd() throws Exception {
		// Expect one SyslogConnection message to be broadcast on the messaging channel
		MockEndpoint broadcastSyslog = getMockEndpoint("mock:queuingservice:broadcastSyslog", false);
		broadcastSyslog.setExpectedMessageCount(1);

		MockEndpoint syslogHandler = getMockEndpoint("mock:seda:syslogHandler", false);
		syslogHandler.setExpectedMessageCount(1);

		// Create a mock SyslogdConfig
		SyslogConfigBean config = new SyslogConfigBean();
		config.setSyslogPort(10514);
		config.setNewSuspectOnMessage(false);

		// Leave a bunch of config that won't be available on the Minion side blank
		//config.setParser("org.opennms.netmgt.syslogd.CustomSyslogParser");
		//config.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
		//config.setMatchingGroupHost(6);
		//config.setMatchingGroupMessage(8);
		//config.setDiscardUei("DISCARD-MATCHING-MESSAGES");

		byte[] messageBytes = "<34>main: 2010-08-19 localhost foo0: load test 0 on tty1\0".getBytes("US-ASCII");

		UUID systemId = UUID.randomUUID();

		// Send a SyslogConnection
		template.sendBody(
			"queuingservice:broadcastSyslog",
			JaxbUtils.marshal(new SyslogConnection(InetAddressUtils.ONE_TWENTY_SEVEN, 2000, ByteBuffer.wrap(messageBytes), config, systemId.toString()))
		);

		assertMockEndpointsSatisfied();

		// Check that the input for the seda:syslogHandler endpoint matches
		// the SyslogConnection that we simulated via ActiveMQ
		SyslogConnection result = syslogHandler.getReceivedExchanges().get(0).getIn().getBody(SyslogConnection.class);
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, result.getSourceAddress());
		assertEquals(2000, result.getPort());
		assertTrue(Arrays.equals(result.getBytes(), messageBytes));
		assertEquals(systemId.toString(), result.getSystemId());

		// Assert that the SyslogdConfig has been updated to the local copy
		// that has been provided as an OSGi service
		assertEquals("DISCARD-MATCHING-MESSAGES", result.getConfig().getDiscardUei());
		assertEquals(4, result.getConfig().getMatchingGroupHost());
		assertEquals(7, result.getConfig().getMatchingGroupMessage());
	}
}
