/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import org.opennms.features.kafka.producer.datasync.KafkaAlarmDataSync;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class OpennmsKafkaProducer implements AlarmLifecycleListener, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(OpennmsKafkaProducer.class);
    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();

    public static final String KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client";
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private final ProtobufMapper protobufMapper;
    private final NodeCache nodeCache;
    private final ConfigurationAdmin configAdmin;
    private final EventSubscriptionService eventSubscriptionService;
    private KafkaAlarmDataSync dataSync;

    private String eventTopic;
    private String alarmTopic;
    private String nodeTopic;

    private boolean forwardEvents;
    private boolean forwardAlarms;
    private boolean suppressIncrementalAlarms;
    private boolean forwardNodes;
    private Expression eventFilterExpression;
    private Expression alarmFilterExpression;

    private final CountDownLatch forwardedEvent = new CountDownLatch(1);
    private final CountDownLatch forwardedAlarm = new CountDownLatch(1);
    private final CountDownLatch forwardedNode = new CountDownLatch(1);

    private KafkaProducer<String, byte[]> producer;
    
    private final Map<String, OpennmsModelProtos.Alarm> outstandingAlarms = new ConcurrentHashMap<>();
    private final AlarmEqualityChecker alarmEqualityChecker =
            AlarmEqualityChecker.with(AlarmEqualityChecker.Exclusions::defaultExclusions);

    private int kafkaSendQueueCapacity;
    private BlockingQueue<KafkaRecord> kafkaSendQueue;
    private final ExecutorService kafkaSendQueueExecutor =
            Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "KafkaSendQueueProcessor"));

    public OpennmsKafkaProducer(ProtobufMapper protobufMapper, NodeCache nodeCache,
                                ConfigurationAdmin configAdmin, EventSubscriptionService eventSubscriptionService) {
        this.protobufMapper = Objects.requireNonNull(protobufMapper);
        this.nodeCache = Objects.requireNonNull(nodeCache);
        this.configAdmin = Objects.requireNonNull(configAdmin);
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
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
        // Overwrite the serializers, since we rely on these
        producerConfig.put("key.serializer", StringSerializer.class.getCanonicalName());
        producerConfig.put("value.serializer", ByteArraySerializer.class.getCanonicalName());

        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Class-loader hack for accessing the org.apache.kafka.common.serialization.*
            Thread.currentThread().setContextClassLoader(null);
            producer = new KafkaProducer<>(producerConfig);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        // Start processing records that have been queued for sending
        if(kafkaSendQueueCapacity <= 0) {
            kafkaSendQueueCapacity = 1000;
            LOG.info("Defaulted the 'kafkaSendQueueCapacity' to 1000 since no property was set");
        }
        
        kafkaSendQueue = new LinkedBlockingQueue<>(kafkaSendQueueCapacity);
        kafkaSendQueueExecutor.execute(this::processKafkaSendQueue);

        if (forwardEvents) {
            eventSubscriptionService.addEventListener(this);
        }
    }

    public void destroy() {
        kafkaSendQueueExecutor.shutdownNow();

        if (producer != null) {
            producer.close();
            producer = null;
        }

        if (forwardEvents) {
            eventSubscriptionService.removeEventListener(this);
        }
    }

    private void forwardEvent(Event event) {
        boolean shouldForwardEvent = true;
        // Filtering
        if (eventFilterExpression != null) {
            try {
                shouldForwardEvent = eventFilterExpression.getValue(event, Boolean.class);
            } catch (Exception e) {
                LOG.error("Event filter '{}' failed to return a result for event: {}. The event will be forwarded anyways.",
                        eventFilterExpression.getExpressionString(), event.toStringSimple(), e);
            }
        }
        if (!shouldForwardEvent) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Event {} not forwarded due to event filter: {}",
                        event.toStringSimple(), eventFilterExpression.getExpressionString());
            }
            return;
        }

        // Node handling
        if (forwardNodes && event.getNodeid() != null && event.getNodeid() != 0) {
            maybeUpdateNode(event.getNodeid());
        }

        // Forward!
        sendRecord(() -> {
            final OpennmsModelProtos.Event mappedEvent = protobufMapper.toEvent(event).build();
            LOG.debug("Sending event with UEI: {}", mappedEvent.getUei());
            return new ProducerRecord<>(eventTopic, mappedEvent.getUei(), mappedEvent.toByteArray());
        }, recordMetadata -> {
            // We've got an ACK from the server that the event was forwarded
            // Let other threads know when we've successfully forwarded an event
            forwardedEvent.countDown();
        });
    }

    public boolean shouldForwardAlarm(OnmsAlarm alarm) {
        if (alarmFilterExpression != null) {
            // The expression is not necessarily thread safe
            synchronized(this) {
                try {
                    final boolean shouldForwardAlarm = alarmFilterExpression.getValue(alarm, Boolean.class);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Alarm {} not forwarded due to event filter: {}",
                                alarm, alarmFilterExpression.getExpressionString());
                    }
                    return shouldForwardAlarm;
                } catch (Exception e) {
                    LOG.error("Alarm filter '{}' failed to return a result for event: {}. The alarm will be forwarded anyways.",
                            alarmFilterExpression.getExpressionString(), alarm, e);
                }
            }
        }
        return true;
    }

    private boolean isIncrementalAlarm(String reductionKey, OnmsAlarm alarm) {
        OpennmsModelProtos.Alarm existingAlarm = outstandingAlarms.get(reductionKey);
        return existingAlarm != null && alarmEqualityChecker.equalsExcludingOnFirst(protobufMapper.toAlarm(alarm),
                existingAlarm);
    }

    private void recordIncrementalAlarm(String reductionKey, OnmsAlarm alarm) {
        // Apply the excluded fields when putting to the map so we do not have to perform this calculation
        // on each equality check
        outstandingAlarms.put(reductionKey,
                AlarmEqualityChecker.Exclusions.defaultExclusions(protobufMapper.toAlarm(alarm)).build());
    }

    private void updateAlarm(String reductionKey, OnmsAlarm alarm) {
        // Always push null records, no good way to perform filtering on these
        if (alarm == null) {
            // The alarm has been deleted so we shouldn't track it in the map of outstanding alarms any longer
            outstandingAlarms.remove(reductionKey);
            
            // The alarm was deleted, push a null record to the reduction key
            sendRecord(() -> {
                LOG.debug("Deleting alarm with reduction key: {}", reductionKey);
                return new ProducerRecord<>(alarmTopic, reductionKey, null);
            }, recordMetadata -> {
                // We've got an ACK from the server that the alarm was forwarded
                // Let other threads know when we've successfully forwarded an alarm
                forwardedAlarm.countDown();
            });
            return;
        }

        // Filtering
        if (!shouldForwardAlarm(alarm)) {
            return;
        }

        if (suppressIncrementalAlarms && isIncrementalAlarm(reductionKey, alarm)) {
            return;
        }

        // Node handling
        if (forwardNodes && alarm.getNodeId() != null) {
            maybeUpdateNode(alarm.getNodeId());
        }

        // Forward!
        sendRecord(() -> {
            final OpennmsModelProtos.Alarm mappedAlarm = protobufMapper.toAlarm(alarm).build();
            LOG.debug("Sending alarm with reduction key: {}", reductionKey);
            if (suppressIncrementalAlarms) {
                recordIncrementalAlarm(reductionKey, alarm);
            }
            return new ProducerRecord<>(alarmTopic, reductionKey, mappedAlarm.toByteArray());
        }, recordMetadata -> {
            // We've got an ACK from the server that the alarm was forwarded
            // Let other threads know when we've successfully forwarded an alarm
            forwardedAlarm.countDown();
        });
    }

    private void maybeUpdateNode(long nodeId) {
        nodeCache.triggerIfNeeded(nodeId, (node) -> {
            final String nodeCriteria;
            if (node != null && node.getForeignSource() != null && node.getForeignId() != null) {
                nodeCriteria = String.format("%s:%s", node.getForeignSource(), node.getForeignId());
            } else {
                nodeCriteria = Long.toString(nodeId);
            }

            if (node == null) {
                // The node was deleted, push a null record
                sendRecord(() -> {
                    LOG.debug("Deleting node with criteria: {}", nodeCriteria);
                    return new ProducerRecord<>(nodeTopic, nodeCriteria, null);
                });
                return;
            }

            sendRecord(() -> {
                final OpennmsModelProtos.Node mappedNode = protobufMapper.toNode(node).build();
                LOG.debug("Sending node with criteria: {}", nodeCriteria);
                return new ProducerRecord<>(nodeTopic, nodeCriteria, mappedNode.toByteArray());
            }, recordMetadata -> {
                // We've got an ACK from the server that the node was forwarded
                // Let other threads know when we've successfully forwarded a node
                forwardedNode.countDown();
            });
        });
    }

    private void sendRecord(Callable<ProducerRecord<String,byte[]>> callable) {
        sendRecord(callable, null);
    }

    private void sendRecord(Callable<ProducerRecord<String, byte[]>> callable, Consumer<RecordMetadata> callback) {
        if (producer == null) {
            return;
        }

        final ProducerRecord<String, byte[]> record;
        try {
            record = callable.call();
        } catch (Exception e) {
            // Propagate
            throw new RuntimeException(e);
        }

        // Rather than attempt to send, we instead queue the record to avoid blocking since KafkaProducer's send()
        // method can block if Kafka is not available when metadata is attempted to be retrieved

        // Any offer that fails due to capacity overflow will simply be dropped and will have to wait until the next
        // sync to be processed so this is just a best effort attempt
        if (!kafkaSendQueue.offer(new KafkaRecord(record, callback))) {
            RATE_LIMITED_LOGGER.warn("Dropped a Kafka record due to queue capacity being full.");
        }
    }

    private void processKafkaSendQueue() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                KafkaRecord kafkaRecord = kafkaSendQueue.take();
                ProducerRecord<String, byte[]> producerRecord = kafkaRecord.getProducerRecord();
                Consumer<RecordMetadata> consumer = kafkaRecord.getConsumer();

                try {
                    producer.send(producerRecord, (recordMetadata, e) -> {
                        if (e != null) {
                            LOG.warn("Failed to send record to producer: {}.", producerRecord, e);
                            return;
                        }
                        if (consumer != null) {
                            consumer.accept(recordMetadata);
                        }
                    });
                } catch (RuntimeException e) {
                    LOG.warn("Failed to send record to producer: {}.", producerRecord, e);
                }
            } catch (InterruptedException ignore) {
                break;
            }
        }
    }

    @Override
    public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        if (!forwardAlarms || dataSync == null) {
            // Ignore
            return;
        }
        
        // Remove any outstanding alarms that are not present in the snapshot
        Set<String> reductionKeysInSnapshot = alarms.stream()
                .map(OnmsAlarm::getReductionKey)
                .collect(Collectors.toSet());
        outstandingAlarms.keySet().removeIf(reductionKey -> !reductionKeysInSnapshot.contains(reductionKey));

        dataSync.handleAlarmSnapshot(alarms);
    }

    @Override
    public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        if (!forwardAlarms) {
            // Ignore
            return;
        }
        updateAlarm(alarm.getReductionKey(), alarm);
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
        if (!forwardAlarms) {
            // Ignore
            return;
        }
        handleDeletedAlarm(reductionKey);
    }

    public void handleDeletedAlarm(String reductionKey) {
        updateAlarm(reductionKey, null);
    }

    @Override
    public String getName() {
        return OpennmsKafkaProducer.class.getName();
    }

    @Override
    public void onEvent(Event event) {
        forwardEvent(event);
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
        forwardEvents = !Strings.isNullOrEmpty(eventTopic);
    }

    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
        forwardAlarms = !Strings.isNullOrEmpty(alarmTopic);
    }

    public void setNodeTopic(String nodeTopic) {
        this.nodeTopic = nodeTopic;
        forwardNodes = !Strings.isNullOrEmpty(nodeTopic);
    }

    public void setEventFilter(String eventFilter) {
        if (Strings.isNullOrEmpty(eventFilter)) {
            eventFilterExpression = null;
        } else {
            eventFilterExpression = SPEL_PARSER.parseExpression(eventFilter);
        }
    }

    public void setAlarmFilter(String alarmFilter) {
        if (Strings.isNullOrEmpty(alarmFilter)) {
            alarmFilterExpression = null;
        } else {
            alarmFilterExpression = SPEL_PARSER.parseExpression(alarmFilter);
        }
    }

    public OpennmsKafkaProducer setDataSync(KafkaAlarmDataSync dataSync) {
        this.dataSync = dataSync;
        return this;
    }

    public boolean isForwardingAlarms() {
        return forwardAlarms;
    }

    public CountDownLatch getEventForwardedLatch() {
        return forwardedEvent;
    }

    public CountDownLatch getAlarmForwardedLatch() {
        return forwardedAlarm;
    }

    public CountDownLatch getNodeForwardedLatch() {
        return forwardedNode;
    }

    public void setSuppressIncrementalAlarms(boolean suppressIncrementalAlarms) {
        this.suppressIncrementalAlarms = suppressIncrementalAlarms;
    }

    @VisibleForTesting
    KafkaAlarmDataSync getDataSync() {
        return dataSync;
    }

    public void setKafkaSendQueueCapacity(int kafkaSendQueueCapacity) {
        this.kafkaSendQueueCapacity = kafkaSendQueueCapacity;
    }

    private static final class KafkaRecord {
        private final ProducerRecord<String, byte[]> producerRecord;
        private final Consumer<RecordMetadata> consumer;

        KafkaRecord(ProducerRecord<String, byte[]> producerRecord, Consumer<RecordMetadata> consumer) {
            this.producerRecord = producerRecord;
            this.consumer = consumer;
        }

        ProducerRecord<String, byte[]> getProducerRecord() {
            return producerRecord;
        }

        Consumer<RecordMetadata> getConsumer() {
            return consumer;
        }
    }
}
