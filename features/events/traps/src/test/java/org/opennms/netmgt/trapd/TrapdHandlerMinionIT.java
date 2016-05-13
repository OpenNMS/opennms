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

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Component;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
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
public class TrapdHandlerMinionIT extends CamelBlueprintTestSupport {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerMinionIT.class);

	private static BrokerService m_broker = null;

	/**
	 * Use Aries Blueprint synchronous mode to avoid a blueprint deadlock bug.
	 * 
	 * @see https://issues.apache.org/jira/browse/ARIES-1051
	 * @see https://access.redhat.com/site/solutions/640943
	 */
	@Override
	public void doPreSetup() throws Exception {
		System.setProperty("org.apache.aries.blueprint.synchronous", Boolean.TRUE.toString());
		System.setProperty("de.kalpatec.pojosr.framework.events.sync", Boolean.TRUE.toString());
	}

	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

	@Override
	public boolean isUseDebugger() {
		// must enable debugger
		return true;
	}

	@Override
	public String isMockEndpoints() {
		return "*";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {

		// Create a mock TrapdConfigBean
		TrapdConfigBean config = new TrapdConfigBean();
		config.setSnmpTrapPort(10514);
		config.setSnmpTrapAddress("127.0.0.1");
		config.setNewSuspectOnTrap(false);

		services.put(TrapdConfig.class.getName(), new KeyValueHolder<Object, Dictionary>(config, new Properties()));

		Properties props = new Properties();
		props.setProperty("alias", "opennms.broker");

		//creating the Active MQ component and service
		ActiveMQComponent activeMQ = new ActiveMQComponent();
		activeMQ.setBrokerURL("tcp://127.0.0.1:61716");
		services.put( Component.class.getName(), new KeyValueHolder<Object, Dictionary>( activeMQ, props ) );
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-minion.xml";
	}

	@BeforeClass
	public static void startActiveMQ() throws Exception {
		m_broker = new BrokerService();
		// TODO: Open the broker on a port that is checked to be free
		m_broker.addConnector("tcp://127.0.0.1:61716");
		m_broker.start();
	}

	@AfterClass
	public static void stopActiveMQ() throws Exception {
		if (m_broker != null) {
			m_broker.stop();
		}
	}

	@Test
	public void testTrapd() throws Exception {
		MockEndpoint broadcastTrap = getMockEndpoint("mock:queuingservice:broadcastTrap", false);
		broadcastTrap.setExpectedMessageCount(1);

		PDU snmp4JV2cTrapPdu = new PDU();

		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Major")));
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);

		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				InetAddressUtils.ONE_TWENTY_SEVEN, "public",
				snmp4JV2cTrapPdu, new BasicTrapProcessor());

		template.requestBody("seda:handleMessage", snmp4JV2cTrap);

		assertMockEndpointsSatisfied();

		TrapNotification received = broadcastTrap.getExchanges().get(0).getIn().getBody(TrapNotification.class);
		BasicTrapProcessor receivedProcessor = (BasicTrapProcessor)received.getTrapProcessor();
		assertEquals("public", receivedProcessor.getCommunity());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, receivedProcessor.getTrapAddress());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, receivedProcessor.getAgentAddress());
		assertEquals(".1.3.6.1.2.1.1.3", receivedProcessor.getTrapIdentity().getEnterpriseId());
		assertEquals(6, receivedProcessor.getTrapIdentity().getGeneric());
		assertEquals(0, receivedProcessor.getTrapIdentity().getSpecific());
	}
}
