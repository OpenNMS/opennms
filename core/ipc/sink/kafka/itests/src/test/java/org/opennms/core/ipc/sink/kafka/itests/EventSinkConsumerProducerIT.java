/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.itests;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.sink.EventModule;
import org.opennms.netmgt.eventd.sink.EventSinkConsumer;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Malatesh Sudarshan
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml" })
@JUnitConfigurationEnvironment
public class EventSinkConsumerProducerIT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer(tempFolder);

    private SinkKafkaProducer kafkaProducer;

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    private static final String EVENT_SOURCE = "trapd";

    private static final String LOCATION = "Default";

    @Autowired
    private MockEventIpcManager mockIpcManager;

    @Autowired
    private EventdConfig m_config;

    private EventSinkConsumer m_eventSinkConsumer;

    private EventModule m_eventsModule;

    private final EventAnticipator m_anticipator = new EventAnticipator();

    @Before
    public void setUp() throws Exception {
        
        m_eventsModule = new EventModule(m_config);
        m_eventSinkConsumer = new EventSinkConsumer();
        mockIpcManager.addEventListener(m_anticipator);
        m_eventSinkConsumer.setconfig(m_config);
        m_eventSinkConsumer.setEventForwarder(mockIpcManager);
        m_eventSinkConsumer.setMessageConsumerManager(consumerManager);

        File data = tempFolder.newFolder("data");
        Hashtable<String, Object> producerConfig = new Hashtable<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                           kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class,
                                              RETURNS_DEEP_STUBS);
        Hashtable<String, Object> streamsConfig = new Hashtable<>();
        streamsConfig.put(StreamsConfig.STATE_DIR_CONFIG,
                          data.getAbsolutePath());
        streamsConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfig.put(StreamsConfig.METADATA_MAX_AGE_CONFIG, 1000);
        when(configAdmin.getConfiguration(SinkKafkaProducer.KAFKA_CLIENT_PID).getProperties()).thenReturn(producerConfig);

        kafkaProducer = new SinkKafkaProducer(configAdmin);

        System.setProperty(String.format("%sbootstrap.servers",
                                         org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset",
                                         KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           "earliest");
        consumerManager.afterPropertiesSet();

        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX,
                                                                             m_eventsModule.getId());
        kafkaProducer.setEventTopic(topicNameFactory.getName());
        kafkaProducer.init();
    }

    private Log getEventLog() throws UnknownHostException {
        EventBuilder eventBldr = new EventBuilder(org.opennms.netmgt.events.api.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI,
                                                  EVENT_SOURCE);
        eventBldr.setInterface(InetAddress.getLocalHost());
        eventBldr.setHost(InetAddressUtils.getLocalHostName());
        eventBldr.setDistPoller(LOCATION);
        Log messageLog = new Log();
        messageLog.addEvent(eventBldr.getEvent());
        return messageLog;
    }

    @Test
    public void canProduceAndConsumeMessages() throws Exception {
        try {
            
            consumerManager.registerConsumer(m_eventSinkConsumer);

            await().atMost(1,
                           MINUTES).until(() -> m_anticipator.getUnanticipatedEvents().size(),
                                          equalTo(1));

            final List<Event> events = m_anticipator.getUnanticipatedEvents();
            assertNotNull(events.get(0));
            Event event = events.get(0);
            assertEquals(1, events.size());
            assertEquals(event.getUei(),
                         EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
            assertEquals(event.getDistPoller(), LOCATION);
            assertEquals(event.getSource(), EVENT_SOURCE);
        } finally {
            consumerManager.unregisterConsumer(m_eventSinkConsumer);
        }

    }

    public class SinkKafkaProducer  {

        public static final String KAFKA_CLIENT_PID = "org.opennms.features.sink.kafka.producer.client";

        private final ConfigurationAdmin configAdmin;

        private String eventTopic;

        private KafkaProducer<String, byte[]> producer;

        public SinkKafkaProducer(ConfigurationAdmin configAdmin) {
            this.configAdmin = Objects.requireNonNull(configAdmin);
        }

        public void init() throws IOException {
            // Create the Kafka producer
            try {
                final Properties producerConfig = new Properties();
                final Dictionary<String, Object> properties = configAdmin.getConfiguration(KAFKA_CLIENT_PID).getProperties();
                if (properties != null) {
                    final Enumeration<String> keys = properties.keys();
                    while (keys.hasMoreElements()) {
                        final String key = keys.nextElement();
                        producerConfig.put(key, properties.get(key));
                    }
                }
                producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                   StringSerializer.class.getCanonicalName());
                producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                   ByteArraySerializer.class.getCanonicalName());

                final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(null);
                    producer = new KafkaProducer<>(producerConfig);
                    kafkaProducer.forwardEvent(m_eventsModule.marshal(getEventLog()));
                } finally {
                    Thread.currentThread().setContextClassLoader(currentClassLoader);
                }
            } 
            finally {
                destroy();
            }
        }

        public void destroy() {
            if (producer != null) {
                producer.close();
                producer = null;
            }

        }

        public void setEventTopic(String eventTopicName) {
            eventTopic = eventTopicName;

        }

        public void forwardEvent(byte[] event) {
            producer.send(new ProducerRecord<>(eventTopic, event));

        }

    }

}
