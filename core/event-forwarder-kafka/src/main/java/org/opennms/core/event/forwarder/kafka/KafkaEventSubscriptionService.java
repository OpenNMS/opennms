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
package org.opennms.core.event.forwarder.kafka;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.opennms.features.events.kafka.consumer.EventDeserializer;
import org.opennms.features.events.kafka.consumer.XmlEventDeserializer;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link EventSubscriptionService} backed by a Kafka consumer.
 *
 * <p>Polls a single Kafka topic, deserializes each record into an {@link Event},
 * converts it to an immutable {@link IEvent}, and dispatches to registered
 * {@link EventListener}s using the same wildcard prefix-matching algorithm as
 * {@code EventIpcManagerDefaultImpl.broadcastNow()}.</p>
 */
public class KafkaEventSubscriptionService implements EventSubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventSubscriptionService.class);

    private final KafkaConsumer<Long, byte[]> consumer;
    private final String topicName;
    private final EventDeserializer deserializer;
    private final Duration pollTimeout;

    /** Listeners interested in all events (no UEI filter). */
    private final CopyOnWriteArrayList<EventListener> allEventsListeners = new CopyOnWriteArrayList<>();

    /** Listeners registered for specific UEIs (exact or prefix). */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<EventListener>> ueiListeners = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread pollThread;

    public KafkaEventSubscriptionService(
            KafkaConsumer<Long, byte[]> consumer,
            String topicName,
            EventDeserializer deserializer,
            Duration pollTimeout) {
        this.consumer = Objects.requireNonNull(consumer, "consumer must not be null");
        this.topicName = Objects.requireNonNull(topicName, "topicName must not be null");
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer must not be null");
        this.pollTimeout = Objects.requireNonNull(pollTimeout, "pollTimeout must not be null");
    }

    /**
     * Factory method for Blueprint. Takes only String/long primitives,
     * creating the KafkaConsumer and EventDeserializer internally to avoid
     * Aries Blueprint constructor type-matching issues with cross-bundle types.
     */
    public static KafkaEventSubscriptionService create(
            String bootstrapServers,
            String consumerGroupId,
            String topicName,
            long pollTimeoutMs) {
        KafkaConsumer<Long, byte[]> consumer = KafkaConsumerFactory.create(bootstrapServers, consumerGroupId);
        EventDeserializer deserializer = new XmlEventDeserializer();
        return new KafkaEventSubscriptionService(consumer, topicName, deserializer, Duration.ofMillis(pollTimeoutMs));
    }

    /**
     * Subscribes the Kafka consumer to the configured topic and starts the
     * daemon poll thread.
     */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            LOG.warn("KafkaEventSubscriptionService is already running");
            return;
        }
        consumer.subscribe(Collections.singletonList(topicName));
        pollThread = new Thread(this::pollLoop, "kafka-event-subscription-poll");
        pollThread.setDaemon(true);
        pollThread.start();
        LOG.info("KafkaEventSubscriptionService started, polling topic '{}'", topicName);
    }

    /**
     * Signals the poll loop to stop, wakes up the Kafka consumer, and waits
     * for the poll thread to terminate.
     */
    public void stop() throws InterruptedException {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        consumer.wakeup();
        if (pollThread != null) {
            pollThread.join(10_000);
        }
        LOG.info("KafkaEventSubscriptionService stopped");
    }

    // -------- EventSubscriptionService implementation --------

    @Override
    public void addEventListener(EventListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        allEventsListeners.addIfAbsent(listener);
        LOG.debug("Added all-events listener: {}", listener.getName());
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        Objects.requireNonNull(listener, "listener must not be null");
        Objects.requireNonNull(ueis, "ueis must not be null");
        for (String uei : ueis) {
            addEventListener(listener, uei);
        }
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        Objects.requireNonNull(listener, "listener must not be null");
        Objects.requireNonNull(uei, "uei must not be null");
        ueiListeners.computeIfAbsent(uei, k -> new CopyOnWriteArrayList<>()).addIfAbsent(listener);
        LOG.debug("Added UEI listener '{}' for UEI '{}'", listener.getName(), uei);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        allEventsListeners.remove(listener);
        // Also remove from all UEI-specific registrations
        for (CopyOnWriteArrayList<EventListener> listeners : ueiListeners.values()) {
            listeners.remove(listener);
        }
        LOG.debug("Removed all-events listener: {}", listener.getName());
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        Objects.requireNonNull(listener, "listener must not be null");
        Objects.requireNonNull(ueis, "ueis must not be null");
        for (String uei : ueis) {
            removeEventListener(listener, uei);
        }
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        Objects.requireNonNull(listener, "listener must not be null");
        Objects.requireNonNull(uei, "uei must not be null");
        CopyOnWriteArrayList<EventListener> listeners = ueiListeners.get(uei);
        if (listeners != null) {
            listeners.remove(listener);
            // Clean up empty lists
            if (listeners.isEmpty()) {
                ueiListeners.remove(uei);
            }
        }
        LOG.debug("Removed UEI listener '{}' for UEI '{}'", listener.getName(), uei);
    }

    @Override
    public boolean hasEventListener(String uei) {
        CopyOnWriteArrayList<EventListener> listeners = ueiListeners.get(uei);
        if (listeners != null && !listeners.isEmpty()) {
            return true;
        }
        // Check wildcard prefix matches: progressively shorten the UEI
        for (String prefix = uei; prefix.length() > 0; ) {
            int i = prefix.lastIndexOf("/", prefix.length() - 2);
            if (i > 0) {
                prefix = prefix.substring(0, i + 1);
                listeners = ueiListeners.get(prefix);
                if (listeners != null && !listeners.isEmpty()) {
                    return true;
                }
            } else {
                break;
            }
        }
        return !allEventsListeners.isEmpty();
    }

    // -------- poll loop --------

    private void pollLoop() {
        try {
            while (running.get()) {
                ConsumerRecords<Long, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<Long, byte[]> record : records) {
                    try {
                        Event mutableEvent = deserializer.deserialize(record.value());
                        IEvent immutableEvent = ImmutableMapper.fromMutableEvent(mutableEvent);
                        dispatch(immutableEvent);
                    } catch (Exception e) {
                        LOG.warn("Failed to deserialize or dispatch event from offset {}: {}",
                                record.offset(), e.getMessage(), e);
                    }
                }
            }
        } catch (WakeupException e) {
            // Expected during shutdown — only rethrow if still running
            if (running.get()) {
                throw e;
            }
        } finally {
            try {
                consumer.close();
            } catch (Exception e) {
                LOG.warn("Error closing Kafka consumer: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Dispatches an event to matching listeners using the same wildcard prefix
     * matching algorithm as {@code EventIpcManagerDefaultImpl.broadcastNow()}.
     *
     * <p>Dispatch order:</p>
     * <ol>
     *   <li>All-events listeners (registered with no UEI filter)</li>
     *   <li>Exact UEI match, then progressively shorter prefix matches
     *       (each prefix ending with "/")</li>
     * </ol>
     *
     * <p>A listener is only dispatched once per event, even if it matches
     * multiple registrations.</p>
     */
    private void dispatch(IEvent event) {
        Set<EventListener> dispatched = new HashSet<>();

        // 1. Dispatch to all-events listeners
        for (EventListener listener : allEventsListeners) {
            dispatchToListener(event, listener);
            dispatched.add(listener);
        }

        String uei = event.getUei();
        if (uei == null) {
            LOG.debug("Event has no UEI, skipping UEI-specific dispatch");
            return;
        }

        // 2. Dispatch to UEI-specific listeners using wildcard prefix matching
        for (String matchUei = uei; matchUei.length() > 0; ) {
            List<EventListener> listeners = ueiListeners.get(matchUei);
            if (listeners != null) {
                for (EventListener listener : listeners) {
                    if (!dispatched.contains(listener)) {
                        dispatchToListener(event, listener);
                        dispatched.add(listener);
                    }
                }
            }

            // Try wildcard: find "/" before last character
            int i = matchUei.lastIndexOf("/", matchUei.length() - 2);
            if (i > 0) {
                // Split at "/", including the "/"
                matchUei = matchUei.substring(0, i + 1);
            } else {
                break;
            }
        }

        if (dispatched.isEmpty()) {
            LOG.debug("No listener interested in event UEI: {}", uei);
        }
    }

    private void dispatchToListener(IEvent event, EventListener listener) {
        try {
            listener.onEvent(event);
        } catch (Exception e) {
            LOG.warn("Listener '{}' threw exception processing event UEI '{}': {}",
                    listener.getName(), event.getUei(), e.getMessage(), e);
        }
    }
}
