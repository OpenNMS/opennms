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
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.dao.DistPollerDaoMinion;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
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
public class TrapdHandlerMinionIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerMinionIT.class);

	@ClassRule
	public static ActiveMQBroker s_broker = new ActiveMQBroker();

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {

		// Create a mock TrapdConfigBean
		TrapdConfigBean config = new TrapdConfigBean();
		config.setSnmpTrapPort(10514);
		config.setSnmpTrapAddress("127.0.0.1");
		config.setNewSuspectOnTrap(false);

		services.put(TrapdConfig.class.getName(), new KeyValueHolder<Object, Dictionary>(config, new Properties()));

		OnmsDistPoller distPoller = new OnmsDistPoller();
		distPoller.setId(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLabel(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLocation("localhost");
		DistPollerDao distPollerDao = new DistPollerDaoMinion(distPoller);

		services.put(DistPollerDao.class.getName(), new KeyValueHolder<Object, Dictionary>(distPollerDao, new Properties()));

		//creating the Active MQ component and service
		ActiveMQComponent queueingservice = new ActiveMQComponent();
		queueingservice.setBrokerURL("vm://localhost?create=false");
		Properties props = new Properties();
		props.put("alias", "opennms.broker");
		services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queueingservice, props));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-minion.xml";
	}

	@Test
	public void testTrapd() throws Exception {
		JmsQueueNameFactory factory = new JmsQueueNameFactory("Trapd", "BroadcastTrap");
		MockEndpoint broadcastTrap = getMockEndpoint("mock:queuingservice:" + factory.getName(), false);
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

		String trapDtoXml = broadcastTrap.getReceivedExchanges().get(0).getIn().getBody(String.class);
		assertNotNull(trapDtoXml);
		TrapDTO trapDto = JaxbUtils.unmarshal(TrapDTO.class, trapDtoXml);
		TrapNotification received = TrapDTOToObjectProcessor.dto2object(trapDto);
		// Reset the trap processor since it is a non-serializable, transient field
		received.setTrapProcessor(new BasicTrapProcessor());

		BasicTrapProcessor receivedProcessor = (BasicTrapProcessor)received.getTrapProcessor();
		assertEquals("public", receivedProcessor.getCommunity());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, receivedProcessor.getTrapAddress());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, receivedProcessor.getAgentAddress());
		assertEquals(".1.3.6.1.2.1.1.3", receivedProcessor.getTrapIdentity().getEnterpriseId());
		assertEquals(6, receivedProcessor.getTrapIdentity().getGeneric());
		assertEquals(0, receivedProcessor.getTrapIdentity().getSpecific());
	}
}
