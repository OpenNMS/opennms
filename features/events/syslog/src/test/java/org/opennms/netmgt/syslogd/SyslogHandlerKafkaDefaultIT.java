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

import java.net.ServerSocket;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.util.KeyValueHolder;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogHandlerKafkaDefaultIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(SyslogHandlerKafkaDefaultIT.class);

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

		zkTestServer = new TestingServer(zookeeperPort);
		Properties properties = new Properties();
		properties.put("port", String.valueOf(kafkaPort));
		properties.put("host.name", "localhost");
		properties.put("broker.id", "5001");
		properties.put("enable.zookeeper", "false");
		properties.put("zookeeper.connect",zkTestServer.getConnectString());
		try{
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
		props.put("kafkaAddress", String.valueOf("localhost:" + kafkaPort));
		return "org.opennms.netmgt.syslog.handler.kafka.default";
	}

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

		services.put( SyslogConnectionHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(new SyslogConnectionHandlerCamelImpl("seda:handleMessage"), new Properties()));

		KafkaComponent kafka = new KafkaComponent();
		kafka.createComponentConfiguration().setBaseUri("kafka://localhost:" + kafkaPort);
		services.put( Component.class.getName(), new KeyValueHolder<Object, Dictionary>(kafka, new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-syslog-handler-kafka-default.xml,blueprint-empty-camel-context.xml";
	}

	@Test
	public void testSyslogd() throws Exception {

		SimpleRegistry registry = new SimpleRegistry();
		CamelContext syslogd = new DefaultCamelContext(registry);

		syslogd.addRoutes(new RouteBuilder(){
			@Override
			public void configure() throws Exception {
				from("direct:start").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						exchange.getIn().setBody("Test Message from Camel Kafka Component Final",String.class);
						exchange.getIn().setHeader(KafkaConstants.PARTITION_KEY, 1);
						exchange.getIn().setHeader(KafkaConstants.KEY, "1");
					}
				}).to("kafka:localhost:" + kafkaPort + "?topic=syslog&serializerClass=kafka.serializer.StringEncoder");
			}
		});

		syslogd.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from("kafka:localhost:" + kafkaPort + "?topic=syslog&zookeeperHost=localhost&zookeeperPort=" + zookeeperPort + "&groupId=testing")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						String messageKey = "";
						if (exchange.getIn() != null) {
							Message message = exchange.getIn();
							Integer partitionId = (Integer)message.getHeader(KafkaConstants.PARTITION);
							String topicName = (String) message.getHeader(KafkaConstants.TOPIC);
							if (message.getHeader(KafkaConstants.KEY) != null) {
								messageKey = (String) message.getHeader(KafkaConstants.KEY);
							}
							Object data = message.getBody();

							System.out.println("topicName :: "
									+ topicName + " partitionId :: "
									+ partitionId + " messageKey :: "
									+ messageKey + " message :: "
									+ data + "\n");
						}
					}
				}).to("log:input");

			}
		});

		syslogd.start();
	}

	@After
	public void shutDownKafka(){
		kafkaServer.shutdown();
	}
}
