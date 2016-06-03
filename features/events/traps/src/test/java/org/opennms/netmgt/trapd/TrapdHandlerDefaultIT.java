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

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

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
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockEventIpcManager.EmptyEventConfDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdHandlerDefaultIT extends CamelBlueprintTest {

	private boolean mockInitialized = false;

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerDefaultIT.class);

	@ClassRule
	public static ActiveMQBroker s_broker = new ActiveMQBroker();

	private EventIpcManager m_eventIpcManager = new MockEventIpcManager();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected String useOverridePropertiesWithConfigAdmin(Dictionary props) {
		props.put("brokerUri", "vm:localhost?create=false");
		// Return the PID
		return "org.opennms.netmgt.trapd.handler.default";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {

		// Create a mock TrapdConfigBean
		TrapdConfigBean config = new TrapdConfigBean();
		//config.setSnmpTrapPort(10514);
		//config.setSnmpTrapAddress("127.0.0.1");
		config.setNewSuspectOnTrap(false);

		services.put(
			TrapdConfig.class.getName(),
			new KeyValueHolder<Object, Dictionary>(config, new Properties())
		);

		services.put(
			EventForwarder.class.getName(),
			new KeyValueHolder<Object, Dictionary>(new EventForwarder() {
				@Override
				public void sendNow(Log eventLog) {
					// Do nothing
					LOG.info("Got an event log: " + eventLog.toString());
				}

				@Override
				public void sendNow(Event event) {
					// Do nothing
					LOG.info("Got an event: " + event.toString());
				}
			}, new Properties())
		);

		services.put(EventConfDao.class.getName(), new KeyValueHolder<Object, Dictionary>(new EmptyEventConfDao(), new Properties()));
        Properties props = new Properties();
        props.setProperty("alias", "onms.broker");
        services.put(Component.class.getName(),
                     new KeyValueHolder<Object, Dictionary>(ActiveMQComponent.activeMQComponent("vm://localhost?create=false"),
                                                            props));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-default.xml";
	}

	@Test
	public void testTrapd() throws Exception {
		// Expect one TrapNotification message to be broadcast on the messaging channel
		MockEndpoint broadcastTrapd = getMockEndpoint("mock:queuingservice:broadcastTrap", false);
		broadcastTrapd.setExpectedMessageCount(1);

		MockEndpoint trapHandler = getMockEndpoint("mock:seda:trapHandler", false);
		trapHandler.setExpectedMessageCount(1);

		/*
		MockTrapdIpMgr m_trapdIpMgr=new MockTrapdIpMgr();

		m_trapdIpMgr.clearKnownIpsMap();
		m_trapdIpMgr.setNodeId("127.0.0.1", 1);
		*/

		// create instance of snmp4JV2cTrap
		PDU snmp4JV2cTrapPdu = new PDU();
		
		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Major")));
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);

		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				InetAddressUtils.ONE_TWENTY_SEVEN, new String("public"),
				snmp4JV2cTrapPdu, new BasicTrapProcessor());

		// Send the TrapNotification
		template.sendBody("queuingservice:broadcastTrap?disableReplyTo=true", snmp4JV2cTrap);

		assertMockEndpointsSatisfied();

		// Check that the input for the seda:trapHandler endpoint matches
		// the TrapQProcessor that we simulated via ActiveMQ
		TrapNotification result = trapHandler.getReceivedExchanges().get(0).getIn().getBody(TrapNotification.class);
		LOG.info("Result: " + result);

		// Assert that the trap's content is correct
		BasicTrapProcessor processor = (BasicTrapProcessor)result.getTrapProcessor();
		assertEquals("v2", processor.getVersion());

		// TODO: Use an EventAnticipator to assert that a trap event was created
	}
}
