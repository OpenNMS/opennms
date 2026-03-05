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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.events.kafka.consumer.EventDeserializer;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;

/**
 * TDD tests for {@link KafkaEventSubscriptionService}.
 *
 * Uses a mock KafkaConsumer that returns predefined ConsumerRecords, then
 * returns empty records on subsequent polls. A trivial EventDeserializer
 * decodes the byte payload as a UEI string and sets it on a new Event.
 */
public class KafkaEventSubscriptionServiceTest {

    private static final String TOPIC = "opennms-fault-events";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    @SuppressWarnings("unchecked")
    private final KafkaConsumer<Long, byte[]> mockConsumer = mock(KafkaConsumer.class);

    /** Simple deserializer: treats bytes as a UEI string. */
    private final EventDeserializer deserializer = data -> {
        Event event = new Event();
        event.setUei(new String(data, StandardCharsets.UTF_8));
        return event;
    };

    private KafkaEventSubscriptionService service;

    @Before
    public void setUp() {
        service = new KafkaEventSubscriptionService(mockConsumer, TOPIC, deserializer, POLL_TIMEOUT);
    }

    @After
    public void tearDown() throws Exception {
        service.stop();
    }

    @Test
    public void shouldDispatchToListenerByUei() throws Exception {
        String targetUei = "uei.opennms.org/nodes/nodeDown";
        ConsumerRecords<Long, byte[]> records = buildRecords(targetUei);
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        service.addEventListener(newListener("test-listener", received, latch), targetUei);
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS))
                .as("Listener should have received the event within 5 seconds")
                .isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).getUei()).isEqualTo(targetUei);
    }

    @Test
    public void shouldNotDispatchUnmatchedUei() throws Exception {
        String publishedUei = "uei.opennms.org/nodes/nodeDown";
        String subscribedUei = "uei.opennms.org/nodes/interfaceDown";

        ConsumerRecords<Long, byte[]> records = buildRecords(publishedUei);
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        service.addEventListener(newListener("mismatch-listener", received, latch), subscribedUei);
        service.start();

        // Give the poll loop time to process at least two polls
        Thread.sleep(500);

        assertThat(received).isEmpty();
    }

    @Test
    public void shouldSupportWildcardPrefixMatching() throws Exception {
        // Subscribe to the "directory" prefix — should match any UEI under that prefix
        String wildcardPrefix = "uei.opennms.org/nodes/";
        String eventUei = "uei.opennms.org/nodes/nodeDown";

        ConsumerRecords<Long, byte[]> records = buildRecords(eventUei);
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        service.addEventListener(newListener("wildcard-listener", received, latch), wildcardPrefix);
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS))
                .as("Wildcard listener should match prefix")
                .isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).getUei()).isEqualTo(eventUei);
    }

    @Test
    public void shouldDispatchToAllEventsListener() throws Exception {
        String eventUei = "uei.opennms.org/anything/goes";

        ConsumerRecords<Long, byte[]> records = buildRecords(eventUei);
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        // addEventListener with no UEI = interested in all events
        service.addEventListener(newListener("all-events-listener", received, latch));
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS))
                .as("All-events listener should receive any event")
                .isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).getUei()).isEqualTo(eventUei);
    }

    @Test
    public void shouldRemoveListener() throws Exception {
        String eventUei = "uei.opennms.org/nodes/nodeDown";

        ConsumerRecords<Long, byte[]> records = buildRecords(eventUei);
        // Return records on every poll so the event is available after the listener is removed
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(ConsumerRecords.empty())  // first poll: empty while we set up
                .thenReturn(ConsumerRecords.empty())   // second poll: still empty
                .thenReturn(records)                   // third poll: records arrive
                .thenReturn(ConsumerRecords.empty());

        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        EventListener listener = newListener("removable-listener", received, latch);
        service.addEventListener(listener, eventUei);

        // Remove the listener before starting (so it never dispatches)
        service.removeEventListener(listener, eventUei);

        service.start();

        // Give the poll loop time to process
        Thread.sleep(500);

        assertThat(received).isEmpty();
    }

    @Test
    public void shouldNotDoubleDispatchToSameListener() throws Exception {
        // A listener subscribes to both a specific UEI and all-events.
        // It should only receive the event once (the all-events path dispatches first,
        // then the UEI-specific path should skip since the listener was already dispatched).
        String eventUei = "uei.opennms.org/nodes/nodeDown";

        ConsumerRecords<Long, byte[]> records = buildRecords(eventUei);
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = newListener("dual-registered-listener", received, latch);
        service.addEventListener(listener);          // all-events
        service.addEventListener(listener, eventUei); // also UEI-specific

        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        // Wait a bit to ensure no second dispatch
        Thread.sleep(300);
        assertThat(received).hasSize(1);
    }

    @Test
    public void shouldReportHasEventListener() {
        String uei = "uei.opennms.org/nodes/nodeDown";
        assertThat(service.hasEventListener(uei)).isFalse();

        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();
        EventListener listener = newListener("has-listener-test", received, new CountDownLatch(1));
        service.addEventListener(listener, uei);

        assertThat(service.hasEventListener(uei)).isTrue();

        service.removeEventListener(listener, uei);
        assertThat(service.hasEventListener(uei)).isFalse();
    }

    // -------- helpers --------

    private ConsumerRecords<Long, byte[]> buildRecords(String... ueis) {
        TopicPartition tp = new TopicPartition(TOPIC, 0);
        CopyOnWriteArrayList<ConsumerRecord<Long, byte[]>> recordList = new CopyOnWriteArrayList<>();
        long offset = 0;
        for (String uei : ueis) {
            recordList.add(new ConsumerRecord<>(TOPIC, 0, offset++, 0L, uei.getBytes(StandardCharsets.UTF_8)));
        }
        return new ConsumerRecords<>(Map.of(tp, List.copyOf(recordList)));
    }

    private EventListener newListener(String name, CopyOnWriteArrayList<IEvent> sink, CountDownLatch latch) {
        return new EventListener() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public void onEvent(IEvent e) {
                sink.add(e);
                latch.countDown();
            }
        };
    }
}
