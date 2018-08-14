/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero
 * General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/ For more information contact: OpenNMS(R)
 * Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.kafka.producer;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.eventd.sink.EventsModule;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Malatesh Sudarshan
 */
public class EventSinkKafkaProducerIT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer(tempFolder);

    private SinkKafkaProducer kafkaProducer;

    private static ExecutorService executor;

    private static final String EVENT_TOPIC_NAME = "sink-events";

    private static EventsModule eventsModule;

    private static KafkaMessageConsumerRunner kafkaConsumer;

    private static final String EVENT_SOURCE = "trapd";

    private static final String LOCATION = "Default";

    @Before
    public void setUp() throws IOException {
        System.setProperty("opennms.home", "src/test/resources");
        eventsModule = new EventsModule(new EventdConfigManager());
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
        kafkaProducer.setEventTopic(EVENT_TOPIC_NAME);
        kafkaProducer.init();

    }

    @Test
    public void canProducerAndConsumeMessages() throws Exception {

        kafkaProducer.forwardEvent(eventsModule.marshal(getEventLog()));
        executor = Executors.newSingleThreadExecutor();
        kafkaConsumer = new KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumer);

        await().atMost(1,
                       TimeUnit.MINUTES).until(() -> kafkaConsumer.getEvent(),
                                               not(nullValue()));
        assertNotNull(kafkaConsumer.getEvent());
        assertEquals(kafkaConsumer.getEvent().getUei(),
                     EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
        assertEquals(kafkaConsumer.getEvent().getSource(), EVENT_SOURCE);
        assertEquals(kafkaConsumer.getEvent().getDistPoller(), LOCATION);
    }

    private Log getEventLog() throws UnknownHostException {
        Event event = new Event();
        EventBuilder bldr = new EventBuilder(org.opennms.netmgt.events.api.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI,
                                             EVENT_SOURCE);
        bldr.setInterface(InetAddress.getLocalHost());
        bldr.setHost(InetAddressUtils.getLocalHostName());
        bldr.setDistPoller(LOCATION);
        bldr.setSource(EVENT_SOURCE);
        event = bldr.getEvent();
        Log messageLog = new Log();
        messageLog.addEvent(event);
        return messageLog;
    }

    public static class KafkaMessageConsumerRunner implements Runnable {
        private KafkaConsumer<String, byte[]> consumer;
        private String kafkaConnectString;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private Event event;

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public KafkaMessageConsumerRunner(String kafkaConnectString) {
            this.kafkaConnectString = Objects.requireNonNull(kafkaConnectString);
        }

        public void shutdown() {
            closed.set(true);
        }

        @After
        public void tearDown() {
            if (kafkaConsumer != null) {
                kafkaConsumer.shutdown();
            }
            if (executor != null) {
                executor.shutdown();
            }
        }

        @Override
        public void run() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                      kafkaConnectString);
            props.put(ConsumerConfig.GROUP_ID_CONFIG,
                      EventSinkKafkaProducerIT.class.getCanonicalName() + "-"
                              + UUID.randomUUID().toString());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                      StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                      ByteArrayDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                      Boolean.TRUE.toString());
            props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(EVENT_TOPIC_NAME));

            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.topic().equals(EVENT_TOPIC_NAME)) {
                        Log eventLog = eventsModule.unmarshal(record.value());
                        setEvent(eventLog.getEvents().getEvent(0));
                    }
                }
            }

        }
    }

    public class SinkKafkaProducer {

        public static final String KAFKA_CLIENT_PID = "org.opennms.features.sink.kafka.producer.client";

        private final ConfigurationAdmin configAdmin;

        private String eventTopic;

        private KafkaProducer<String, byte[]> producer;

        public SinkKafkaProducer(ConfigurationAdmin configAdmin) {
            this.configAdmin = Objects.requireNonNull(configAdmin);
        }

        public void init() throws IOException {
            // Create the Kafka producer
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
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
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
