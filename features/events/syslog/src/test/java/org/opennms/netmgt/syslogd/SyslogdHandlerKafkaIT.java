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

import java.net.InetAddress;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogdHandlerKafkaIT extends CamelBlueprintTestSupport {

	private static final Logger LOG = LoggerFactory.getLogger(SyslogdHandlerKafkaIT.class);
	
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
		services.put( SyslogConnectionHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(new SyslogConnectionHandlerCamelImpl("seda:handleMessage"), new Properties()));
		
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
		return "file:blueprint-syslog-handler-kafka.xml";
	}

	@Test
	public void testSyslogd() throws Exception {
		
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port",61616);	
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext syslogd = new DefaultCamelContext(registry);
        syslogd.addComponent("kafka", new KafkaComponent());
        syslogd.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("seda:handleMessage").convertBodyTo(SyslogConnection.class).process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        SyslogConnection connection = new SyslogConnection();
                        connection.setSourceAddress(InetAddress.getByName("www.opennms.com"));
                        exchange.getIn().setBody(connection);
                        exchange.getIn().setHeader(KafkaConstants.PARTITION_KEY,simple("${body.hostname}"));
                    }
                }).log("address:${body.sourceAddress}").log("port: ${body.port}").transform(simple("${body.byteBuffer}")).convertBodyTo(String.class).log(body().toString()).to("kafka:localhost:9092?topic=syslog;serializerClass=kafka.serializer.StringEncoder");
            
            }
        });
            
        
        syslogd.addRoutes(new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				from("kafka:localhost:9092?topic=syslog&zookeeperHost=localhost&zookeeperPort=2181&groupId=group1")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange)
                            throws Exception {
                        String messageKey = "";
                        if (exchange.getIn() != null) {
                            Message message = exchange.getIn();
                            Integer partitionId = (Integer) message
                                    .getHeader(KafkaConstants.PARTITION);
                            String topicName = (String) message
                                    .getHeader(KafkaConstants.TOPIC);
                            if (message.getHeader(KafkaConstants.KEY) != null)
                                messageKey = (String) message
                                        .getHeader(KafkaConstants.KEY);
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
        ProducerTemplate template = syslogd.createProducerTemplate();
        SyslogConnection connection = new SyslogConnection();
        connection.setSourceAddress(InetAddress.getByName("www.opennms.com"));
        template.sendBody( "seda:handleMessage", connection);
        
	}
	
	@After
	public void shutDownKafka(){
		kafkaServer.shutdown();
	}
}
