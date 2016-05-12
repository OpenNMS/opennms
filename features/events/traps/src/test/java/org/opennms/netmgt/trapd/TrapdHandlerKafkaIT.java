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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdHandlerKafkaIT extends CamelBlueprintTestSupport {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerKafkaIT.class);

	private static KafkaConfig kafkaConfig;
	private KafkaServer kafkaServer;
	private  TestingServer zkTestServer;
	
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
		
		zkTestServer = new TestingServer(2181);
    	Properties properties = new Properties();
    	properties.put("port", "9092");
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
	
    @BeforeClass
    public static void startKafka() throws Exception {

    }
	
    @BeforeClass
    public static void startActiveMQ() throws Exception {

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
		// Register any mock OSGi services here
		services.put( TrapNotificationHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(new TrapNotificationHandlerCamelImpl("seda:handleMessage"), new Properties()));
		
		//creating kafka component
		Properties props = new Properties();
		props.setProperty("alias", "opennms.broker");
		
		KafkaComponent kafka = new KafkaComponent();
		kafka.createComponentConfiguration().setBaseUri("kafka://localhost:9092");
        services.put( Component.class.getName(),
                new KeyValueHolder<Object, Dictionary>( kafka, props ) );
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-kafka.xml";
	}

	@Test
	public void testTrapd() throws Exception {
		
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port",61616);	
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext trapd = new DefaultCamelContext(registry);
        trapd.addComponent("kafka", new KafkaComponent());
        trapd.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("seda:handleMessage").convertBodyTo(TrapNotification.class).process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setBody("Test Message from Camel Kafka Component Final",String.class);
                        exchange.getIn().setHeader(KafkaConstants.PARTITION_KEY,simple("${body.hostname}"));
                    }
                }).log("address:${body.sourceAddress}").log("port: ${body.port}").transform(simple("${body.byteBuffer}")).convertBodyTo(String.class).log(body().toString()).to("kafka:localhost:9092?topic=trapd;serializerClass=kafka.serializer.StringEncoder");
            
            }
        });
        
        trapd.start();
	}
	
	@After
	public void shutDownKafka(){
		kafkaServer.shutdown();
	}
}
