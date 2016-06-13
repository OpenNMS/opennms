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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdListenerBlueprintIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdListenerBlueprintIT.class);

	private static final String PORT_NAME="trapd.listen.port";

	private static final String PERSISTANCE_ID="org.opennms.netmgt.trapd";

	private final TrapNotificationLatch m_handler = new TrapNotificationLatch(3);

	private int m_port;

	/**
	 * This method overrides the blueprint property and sets port to 10514 instead of 162
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected String useOverridePropertiesWithConfigAdmin(Dictionary props) throws Exception {
		// TODO: Check that this port is available before using it
		m_port = getAvailablePort(10500, 10900);
		props.put(PORT_NAME, m_port);
		return PERSISTANCE_ID;
	}

	private static int getAvailablePort(int min, int max) {
		for (int i = min; i <= max; i++) {
			try (ServerSocket socket = new ServerSocket(i)) {
				return socket.getLocalPort();
			} catch (Throwable e) {}
		}
		throw new IllegalStateException("Can't find an available network port");
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Register any mock OSGi services here
		services.put(TrapNotificationHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(m_handler, new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-listener.xml,blueprint-empty-camel-context.xml";
	}

	private static class TrapNotificationLatch implements TrapNotificationHandler {
		private final CountDownLatch m_latch;
		private TrapNotification m_last = null;

		public TrapNotificationLatch(int count) {
			m_latch = new CountDownLatch(count);
		}

		@Override
		public void handleTrapNotification(TrapNotification message) {
			LOG.info("Got a trap, decrementing latch");
			m_latch.countDown();
			m_last = message;
		}

		public CountDownLatch getLatch() {
			return m_latch;
		}

		public TrapNotification getLast() {
			return m_last;
		}
}

	@Test
	public void testTrapd() throws Exception {
		Thread.sleep(10000);

		for (int i = 0; i < 3; i++) {
			LOG.info("Sending trap");
			try {
				SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
				pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
				// warmStart
				pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.6.3.1.1.5.2")));
				pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.4.1.5813")));
				pdu.send(InetAddressUtils.str(InetAddressUtils.ONE_TWENTY_SEVEN), m_port, "public");
			} catch (Throwable e) {
				LOG.error(e.getMessage(), e);
			}
			LOG.info("Trap has been sent");
		}
		if (!m_handler.getLatch().await(30, TimeUnit.SECONDS)) {
			fail("Countdown latch failed");
		}
		
		TrapNotification notification = m_handler.getLast();
		notification.setTrapProcessor(new TrapProcessor() {

			@Override
			public void setCommunity(String community) {
				LOG.info("Comparing community");
				assertEquals("public", community);
			}

			@Override
			public void setTimeStamp(long timeStamp) {
				// TODO: Assert something?
			}

			@Override
			public void setVersion(String version) {
				LOG.info("Comparing version");
				assertEquals("v2", version);
			}

			@Override
			public void setAgentAddress(InetAddress agentAddress) {
				LOG.info("Comparing agent address");
				assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, agentAddress);
			}

			@Override
			public void processVarBind(SnmpObjId name, SnmpValue value) {
			}

			@Override
			public void setTrapAddress(InetAddress trapAddress) {
				LOG.info("Comparing trap address");
				assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, trapAddress);
			}

			@Override
			public void setTrapIdentity(TrapIdentity trapIdentity) {
				LOG.info("Comparing trap identity");
				assertEquals(new TrapIdentity(SnmpObjId.get(".1.3.6.1.4.1.5813"), 1, 0).toString(), trapIdentity.toString());
			}
		});
		
		notification.getTrapProcessor();
	}
}