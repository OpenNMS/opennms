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

import java.io.File;
import java.net.ServerSocket;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.KeyValueHolder;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
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
public class TrapdHandlerKafkaIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerKafkaIT.class);

	private static KafkaConfig kafkaConfig;
	private KafkaServer kafkaServer;
	private TestingServer zkTestServer;

	private int kafkaPort;

	private int zookeeperPort;

	private static int getAvailablePort(int min, int max) {
		for (int i = min; i <= max; i++) {
			try (ServerSocket socket = new ServerSocket(i)) {
				return socket.getLocalPort();
			} catch (Throwable e) {}
		}
		throw new IllegalStateException("Can't find an available network port");
	}

	@Override
	public void doPreSetup() throws Exception {
		super.doPreSetup();

		// Delete any existing Kafka log directory
		FileUtils.deleteDirectory(new File("target/kafka-log"));

		zkTestServer = new TestingServer(zookeeperPort);
		Properties properties = new Properties();
		properties.put("broker.id", "5001");
		properties.put("enable.zookeeper", "false");
		properties.put("host.name", "localhost");
		properties.put("log.dir", "target/kafka-log");
		properties.put("port", String.valueOf(kafkaPort));
		properties.put("zookeeper.connect",zkTestServer.getConnectString());
		try {
			kafkaConfig = new KafkaConfig(properties);
			kafkaServer = new KafkaServer(kafkaConfig, null);
			kafkaServer.startup();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected String setConfigAdminInitialConfiguration(Properties props) {
		zookeeperPort = getAvailablePort(2181, 2281);
		kafkaPort = getAvailablePort(9092, 9192);

		props.put("zookeeperport", String.valueOf(zookeeperPort));
		props.put("kafkaport", String.valueOf(kafkaPort));
		return "org.opennms.netmgt.trapd.handler.kafka";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Register any mock OSGi services here
		OnmsDistPoller distPoller = new OnmsDistPoller();
		distPoller.setId(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLabel(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLocation("localhost");
		DistPollerDao distPollerDao = new DistPollerDaoMinion(distPoller);

		services.put(DistPollerDao.class.getName(), new KeyValueHolder<Object, Dictionary>(distPollerDao, new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-kafka.xml";
	}

	@Test
	public void testTrapd() throws Exception {
		MockEndpoint broadcastTrap = getMockEndpoint("mock:kafka:127.0.0.1:" + kafkaPort, false);
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

	@After
	public void shutDownKafka(){
		kafkaServer.shutdown();
	}
}
