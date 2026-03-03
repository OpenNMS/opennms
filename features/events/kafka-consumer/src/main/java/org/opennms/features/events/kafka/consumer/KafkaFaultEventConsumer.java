/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.events.kafka.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumes fault events from a Kafka topic and dispatches them to registered
 * {@link EventListener} instances.
 *
 * This is the read-side counterpart to {@code FaultEventPublisher}. It enables
 * downstream services (e.g., an extracted Alarmd microservice) to consume fault
 * events from Kafka without requiring access to the monolithic event bus.
 */
public class KafkaFaultEventConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFaultEventConsumer.class);

    private final KafkaConsumer<Long, byte[]> consumer;
    private final String topicName;
    private final List<EventListener> listeners;
    private final EventDeserializer deserializer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Duration pollTimeout;

    public KafkaFaultEventConsumer(KafkaConsumer<Long, byte[]> consumer,
                                   String topicName,
                                   List<EventListener> listeners,
                                   EventDeserializer deserializer,
                                   Duration pollTimeout) {
        this.consumer = consumer;
        this.topicName = topicName;
        this.listeners = listeners;
        this.deserializer = deserializer;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(topicName));
        LOG.info("KafkaFaultEventConsumer started, subscribed to topic: {}", topicName);

        while (running.get()) {
            try {
                ConsumerRecords<Long, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<Long, byte[]> record : records) {
                    processRecord(record);
                }
            } catch (Exception e) {
                if (running.get()) {
                    LOG.error("Error polling Kafka topic {}", topicName, e);
                }
            }
        }

        consumer.close();
        LOG.info("KafkaFaultEventConsumer stopped");
    }

    private void processRecord(ConsumerRecord<Long, byte[]> record) {
        try {
            Event mutableEvent = deserializer.deserialize(record.value());
            IEvent immutableEvent = ImmutableMapper.fromMutableEvent(mutableEvent);
            for (EventListener listener : listeners) {
                try {
                    listener.onEvent(immutableEvent);
                } catch (Exception e) {
                    LOG.warn("Listener {} failed processing event from partition={} offset={}",
                            listener.getName(), record.partition(), record.offset(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to deserialize event from Kafka partition={} offset={}",
                    record.partition(), record.offset(), e);
        }
    }

    public void stop() {
        running.set(false);
        consumer.wakeup();
    }
}
