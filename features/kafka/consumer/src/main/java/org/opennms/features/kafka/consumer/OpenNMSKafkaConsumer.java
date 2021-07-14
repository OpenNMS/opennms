/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.kafka.consumer.events.EventsMapper;
import org.opennms.features.kafka.consumer.events.EventsProto;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public class OpenNMSKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSKafkaConsumer.class);

    private final ConfigurationAdmin configAdmin;

    private final EventForwarder eventForwarder;

    private String eventsTopic;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public static final String KAFKA_CLIENT_PID = "org.opennms.features.kafka.consumer.client";

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("kafka-consumer-events-%d")
            .build();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

    public OpenNMSKafkaConsumer(ConfigurationAdmin configAdmin, EventForwarder eventForwarder) {
        this.configAdmin = configAdmin;
        this.eventForwarder = eventForwarder;
    }

    public void init() throws IOException {
        Properties consumerConfig = new Properties();
        // Add default group as OpenNMS Instance ID, allow it be override from client properties.
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, SystemInfoUtils.getInstanceId());
        final Dictionary<String, Object> properties = configAdmin.getConfiguration(KAFKA_CLIENT_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                consumerConfig.put(key, properties.get(key));
            }
        }
        // Overwrite deserializer
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(consumerConfig);
        executorService.execute(kafkaConsumerRunner);
    }

    public void shutdown() {
        closed.set(true);
        executorService.shutdown();
    }

    public void setEventsTopic(String eventsTopic) {
        this.eventsTopic = eventsTopic;
    }


    private class KafkaConsumerRunner implements Runnable {

        private final Properties consumerConfig;

        public KafkaConsumerRunner(Properties properties) {
            consumerConfig = properties;
        }

        @Override
        public void run() {
            KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(consumerConfig), KafkaConsumer.class.getClassLoader());
            if (Strings.isNullOrEmpty(eventsTopic)) {
                LOG.error("EventsTopic is either null or empty, not invoking kafka consumer");
                return;
            }
            consumer.subscribe(Arrays.asList(eventsTopic));
            LOG.info("subscribed to {}", eventsTopic);
            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(java.time.Duration.ofMillis(Long.MAX_VALUE));
                LOG.trace("Received {} records", records.count());
                List<EventsProto.Event> pbEvents = new ArrayList<>();
                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        EventsProto.Event pbEvent = EventsProto.Event.parseFrom(record.value());
                        pbEvents.add(pbEvent);
                        forwardEventsToOpenNMS(pbEvents);
                    } catch (InvalidProtocolBufferException e) {
                        LOG.warn("Error while parsing event with key {}", record.key());
                    }
                }
            }
        }

        void forwardEventsToOpenNMS(List<EventsProto.Event> pbEvents) {
            List<Event> events = EventsMapper.mapProtobufToEvents(pbEvents);
            Log log = new Log();
            events.forEach(log::addEvent);
            // SendNowSync sends events in synchronous mode which puts more backpressure on Kafka.
            eventForwarder.sendNowSync(log);
            log.getEvents().getEventCollection().forEach(event -> {
                LOG.debug(" Event with uei {}, source {} forwarded to OpenNMS", event.getUei(), event.getSource());
            });
        }

    }
}
