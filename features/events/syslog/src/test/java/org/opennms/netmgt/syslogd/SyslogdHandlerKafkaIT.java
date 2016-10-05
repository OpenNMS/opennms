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

import java.io.File;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.KeyValueHolder;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogdHandlerKafkaIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(SyslogdHandlerKafkaIT.class);

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

	@BeforeClass
	public static void startKafka() throws Exception {
	}

	@Override
	protected String setConfigAdminInitialConfiguration(Properties props) {
		zookeeperPort = getAvailablePort(2181, 2281);
		kafkaPort = getAvailablePort(9092, 9192);

		props.put("kafkaAddress", String.valueOf("127.0.0.1:" + kafkaPort));
		return "org.opennms.netmgt.syslog.handler.kafka";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Register any mock OSGi services here
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-syslog-handler-kafka.xml";
	}

	@Test
	public void testSyslogd() throws Exception {

		final MockEndpoint broadcastSyslog = getMockEndpoint("mock:kafka:127.0.0.1:" + kafkaPort, false);
		broadcastSyslog.setExpectedMessageCount(1);

		// Create a mock SyslogdConfig
		final SyslogConfigBean config = new SyslogConfigBean();
		config.setSyslogPort(10514);
		config.setNewSuspectOnMessage(false);

		final byte[] messageBytes = "<34>main: 2010-08-19 localhost foo0: load test 0 on tty1\0".getBytes("US-ASCII");

		final UUID systemId = UUID.randomUUID();

		//ProducerTemplate template = syslogd.createProducerTemplate();
		final SyslogConnection connection = new SyslogConnection(
			InetAddressUtils.ONE_TWENTY_SEVEN,
			2000,
			ByteBuffer.wrap(messageBytes),
			config,
			systemId.toString(),
			MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID
		);
		template.sendBody( "seda:handleMessage", connection);

		assertMockEndpointsSatisfied();

		// Check that the input for the Kafka endpoint matches
		// the SyslogConnection that we created
		final String trapDtoXml = broadcastSyslog.getReceivedExchanges().get(0).getIn().getBody(String.class);
		assertNotNull(trapDtoXml);

		final SyslogConnection result = SyslogDTOToObjectProcessor.dto2object(
			JaxbUtils.unmarshal(SyslogDTO.class, trapDtoXml)
		);
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, result.getSourceAddress());
		assertEquals(2000, result.getPort());
		assertTrue(Arrays.equals(result.getBytes(), messageBytes));
		assertEquals(systemId.toString(), result.getSystemId());
	}

	@After
	public void shutDownKafka(){
		kafkaServer.shutdown();
	}
}
