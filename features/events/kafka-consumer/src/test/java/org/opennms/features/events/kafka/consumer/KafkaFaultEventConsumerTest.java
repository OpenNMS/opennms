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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;

public class KafkaFaultEventConsumerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDeserializeAndDispatchToListeners() throws Exception {
        KafkaConsumer<Long, byte[]> mockConsumer = mock(KafkaConsumer.class);

        Event sourceEvent = new Event();
        sourceEvent.setUei("uei.opennms.org/nodes/nodeDown");
        sourceEvent.setDbid(12345L);
        sourceEvent.setSource("test");

        EventDeserializer deserializer = data -> sourceEvent;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IEvent> received = new AtomicReference<>();

        EventListener listener = new EventListener() {
            @Override
            public String getName() { return "test-listener"; }

            @Override
            public void onEvent(IEvent e) {
                received.set(e);
                latch.countDown();
            }
        };

        TopicPartition tp = new TopicPartition("opennms-fault-events", 0);
        ConsumerRecord<Long, byte[]> record = new ConsumerRecord<>(
                "opennms-fault-events", 0, 0L, 42L, new byte[]{1, 2, 3});
        Map<TopicPartition, List<ConsumerRecord<Long, byte[]>>> recordMap = new HashMap<>();
        recordMap.put(tp, Collections.singletonList(record));
        ConsumerRecords<Long, byte[]> records = new ConsumerRecords<>(recordMap);

        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        KafkaFaultEventConsumer consumer = new KafkaFaultEventConsumer(
                mockConsumer, "opennms-fault-events",
                Collections.singletonList(listener), deserializer,
                Duration.ofMillis(100));

        Thread thread = new Thread(consumer);
        thread.start();
        boolean dispatched = latch.await(5, TimeUnit.SECONDS);
        consumer.stop();
        thread.join(2000);

        assertThat(dispatched).isTrue();
        assertThat(received.get()).isNotNull();
        assertThat(received.get().getUei()).isEqualTo("uei.opennms.org/nodes/nodeDown");
        assertThat(received.get().getDbid()).isEqualTo(12345L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHandleDeserializationErrors() throws Exception {
        KafkaConsumer<Long, byte[]> mockConsumer = mock(KafkaConsumer.class);

        EventDeserializer failingDeserializer = data -> {
            throw new RuntimeException("Bad data");
        };

        EventListener listener = mock(EventListener.class);
        when(listener.getName()).thenReturn("test-listener");

        TopicPartition tp = new TopicPartition("opennms-fault-events", 0);
        ConsumerRecord<Long, byte[]> record = new ConsumerRecord<>(
                "opennms-fault-events", 0, 0L, 42L, new byte[]{1, 2, 3});
        Map<TopicPartition, List<ConsumerRecord<Long, byte[]>>> recordMap = new HashMap<>();
        recordMap.put(tp, Collections.singletonList(record));
        ConsumerRecords<Long, byte[]> records = new ConsumerRecords<>(recordMap);

        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());

        KafkaFaultEventConsumer consumer = new KafkaFaultEventConsumer(
                mockConsumer, "opennms-fault-events",
                Collections.singletonList(listener), failingDeserializer,
                Duration.ofMillis(100));

        Thread thread = new Thread(consumer);
        thread.start();
        Thread.sleep(500);
        consumer.stop();
        thread.join(2000);

        // Verify the consumer survived the deserialization error — no exception propagated
        assertThat(thread.isAlive()).isFalse();
    }
}
