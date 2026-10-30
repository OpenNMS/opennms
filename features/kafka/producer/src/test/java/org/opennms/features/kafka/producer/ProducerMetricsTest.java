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
package org.opennms.features.kafka.producer;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.After;
import org.junit.Test;
import org.opennms.features.kafka.producer.KafkaProducerManager.MessageType;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;

import com.codahale.metrics.MetricRegistry;

public class ProducerMetricsTest {

    private final MetricRegistry registry = new MetricRegistry();
    private OpennmsKafkaProducer kafkaProducer;

    @After
    public void tearDown() {
        if (kafkaProducer != null) {
            kafkaProducer.destroy();
        }
    }

    @Test
    public void namesMetricsByMessageType() {
        assertEquals("messages.alarm_feedback.sent", ProducerMetrics.name(MessageType.ALARM_FEEDBACK, "sent"));
        assertEquals("messages.node.build", ProducerMetrics.name(MessageType.NODE, "build"));
    }

    @Test
    public void countsBuiltQueuedAndAcknowledgedRecords() throws Exception {
        final MockProducer<byte[], byte[]> producer = new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());
        kafkaProducer = start(producer);

        kafkaProducer.handleAlarmFeedback(Collections.nCopies(3, feedback()));

        await().atMost(10, TimeUnit.SECONDS).until(() -> registry.meter("messages.alarm_feedback.sent").getCount() == 3);
        assertEquals(3, registry.timer("messages.alarm_feedback.build").getCount());
        assertEquals(3, registry.histogram("messages.alarm_feedback.size").getCount());
        assertEquals(0, registry.meter("messages.alarm_feedback.failed").getCount());
        assertEquals(0, registry.getGauges().get("messages.sendQueueSize").getValue());
    }

    @Test
    public void countsRecordsKafkaRejects() throws Exception {
        final MockProducer<byte[], byte[]> producer = new MockProducer<>(false, new ByteArraySerializer(), new ByteArraySerializer());
        kafkaProducer = start(producer);

        kafkaProducer.handleAlarmFeedback(Collections.nCopies(2, feedback()));
        await().atMost(10, TimeUnit.SECONDS).until(() -> producer.history().size() == 2);
        while (producer.errorNext(new RuntimeException("broker unavailable"))) {
            // complete every pending send with an error
        }

        assertEquals(2, registry.meter("messages.alarm_feedback.failed").getCount());
        assertEquals(0, registry.meter("messages.alarm_feedback.sent").getCount());
    }

    @Test
    public void countsEventsThatCannotBeRequeuedAfterATimeout() throws Exception {
        final List<Callback> callbacks = new CopyOnWriteArrayList<>();
        final CountDownLatch release = new CountDownLatch(1);
        @SuppressWarnings("unchecked")
        final Producer<byte[], byte[]> producer = mock(Producer.class);
        when(producer.send(any(), any())).thenAnswer(invocation -> {
            callbacks.add(invocation.getArgument(1));
            if (callbacks.size() == 2) {
                // hold the sender thread so the next record stays in the queue
                release.await();
            }
            return null;
        });
        // only records on the event topic are re-queued after a timeout
        kafkaProducer = start(producer, 1, "alarm-feedback");
        try {
            kafkaProducer.handleAlarmFeedback(Collections.singletonList(feedback()));
            kafkaProducer.handleAlarmFeedback(Collections.singletonList(feedback()));
            await().atMost(10, TimeUnit.SECONDS).until(() -> callbacks.size() == 2);
            kafkaProducer.handleAlarmFeedback(Collections.singletonList(feedback()));
            await().atMost(10, TimeUnit.SECONDS).until(() -> (Integer) registry.getGauges().get("messages.sendQueueSize").getValue() == 1);

            callbacks.get(0).onCompletion(null, new TimeoutException("broker offline"));

            assertEquals(1, registry.meter("messages.alarm_feedback.failed").getCount());
            assertEquals(1, registry.counter("messages.alarm_feedback.dropped").getCount());
        } finally {
            release.countDown();
        }
    }

    @Test
    public void reusesTheMetricsOfAMessageType() {
        final ProducerMetrics metrics = new ProducerMetrics(registry);
        assertSame(metrics.build(MessageType.EVENT), metrics.build(MessageType.EVENT));
        assertSame(registry.timer("messages.event.build"), metrics.build(MessageType.EVENT));
    }

    private OpennmsKafkaProducer start(final Producer<byte[], byte[]> producer) throws Exception {
        return start(producer, 10, null);
    }

    private OpennmsKafkaProducer start(final Producer<byte[], byte[]> producer, final int queueCapacity,
                                       final String eventTopic) throws Exception {
        final KafkaProducerManager manager = mock(KafkaProducerManager.class);
        when(manager.hasConfigurationForMessageType(any())).thenReturn(true);
        when(manager.getProducerForMessageType(any())).thenReturn(producer);
        final ProtobufMapper mapper = new ProtobufMapper(mock(EventConfDao.class), mock(HwEntityDao.class),
                mock(SessionUtils.class), mock(NodeDao.class), 1);

        final OpennmsKafkaProducer kafkaProducer = new OpennmsKafkaProducer(mapper, mock(NodeCache.class), manager,
                mock(EventSubscriptionService.class), mock(OnmsTopologyDao.class), 1);
        kafkaProducer.setAlarmFeedbackTopic("alarm-feedback");
        kafkaProducer.setEventTopic(eventTopic);
        kafkaProducer.setKafkaSendQueueCapacity(queueCapacity);
        kafkaProducer.setMetricRegistry(registry);
        kafkaProducer.init();
        return kafkaProducer;
    }

    private static AlarmFeedback feedback() {
        return AlarmFeedback.newBuilder()
                .withSituationKey("situation")
                .withSituationFingerprint("fingerprint")
                .withAlarmKey("alarm")
                .withFeedbackType(AlarmFeedback.FeedbackType.CORRECT)
                .withReason("reason")
                .withUser("admin")
                .withTimestamp(1L)
                .build();
    }
}
