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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class KafkaEventForwarderTest {

    private static final String FAULT_TOPIC = "opennms-fault-events";
    private static final String IPC_TOPIC = "opennms-ipc-events";

    private EventProcessor eventExpander;
    private EventProcessor tsidAssigner;
    private EventClassifier eventClassifier;

    @SuppressWarnings("unchecked")
    private KafkaProducer<Long, byte[]> kafkaProducer = mock(KafkaProducer.class);

    private KafkaEventForwarder forwarder;

    @Before
    public void setUp() {
        eventExpander = mock(EventProcessor.class);
        tsidAssigner = mock(EventProcessor.class);
        eventClassifier = new EventClassifier();
        kafkaProducer = mock(KafkaProducer.class);

        forwarder = new KafkaEventForwarder(
                eventExpander,
                tsidAssigner,
                eventClassifier,
                kafkaProducer,
                FAULT_TOPIC
        );
        forwarder.setIpcTopicName(IPC_TOPIC);
    }

    @Test
    public void shouldPublishFaultEventToKafkaFaultTopic() {
        Event event = createEvent("uei.opennms.org/nodes/nodeDown", 42L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer).send(captor.capture());

        ProducerRecord<Long, byte[]> record = captor.getValue();
        assertThat(record.topic()).isEqualTo(FAULT_TOPIC);
        assertThat(record.key()).isEqualTo(42L);
        assertThat(record.value()).isNotEmpty();
    }

    @Test
    public void shouldPublishIpcEventToKafkaIpcTopic() {
        // Pure IPC: internal UEI not in CROSS_CONTAINER_INTERNAL_UEIS, no alarmData
        Event event = createEvent("uei.opennms.org/internal/provisiond/nodeScanCompleted", 10L);

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer).send(captor.capture());

        ProducerRecord<Long, byte[]> record = captor.getValue();
        assertThat(record.topic()).isEqualTo(IPC_TOPIC);
        assertThat(record.key()).isEqualTo(10L);
        assertThat(record.value()).isNotEmpty();
    }

    @Test
    public void shouldPublishDualEventToBothKafkaTopics() {
        // DUAL: internal UEI in CROSS_CONTAINER_INTERNAL_UEIS (newSuspect is DUAL)
        Event event = createEvent("uei.opennms.org/internal/discovery/newSuspect", 7L);

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer, times(2)).send(captor.capture());

        List<ProducerRecord<Long, byte[]>> records = captor.getAllValues();
        assertThat(records).extracting(ProducerRecord::topic)
                .containsExactly(FAULT_TOPIC, IPC_TOPIC);
    }

    @Test
    public void shouldDropIpcEventWhenIpcTopicNotConfigured() {
        // Create forwarder without IPC topic
        KafkaEventForwarder noIpcForwarder = new KafkaEventForwarder(
                eventExpander, tsidAssigner, eventClassifier, kafkaProducer, FAULT_TOPIC);
        // Don't set ipcTopicName — pure IPC events should be dropped

        Event event = createEvent("uei.opennms.org/internal/provisiond/nodeScanCompleted", 10L);
        noIpcForwarder.sendNow(event);

        // Pure IPC event with no IPC topic → should not publish to any topic
        verify(kafkaProducer, never()).send(any());
    }

    @Test
    public void shouldCallExpanderAndTsidAssignerBeforeRouting() throws Exception {
        Event event = createEvent("uei.opennms.org/nodes/nodeDown", 1L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        verify(eventExpander).process(any(Log.class));
        verify(tsidAssigner).process(any(Log.class));
        verify(kafkaProducer).send(any());
    }

    @Test
    public void shouldUseZeroKeyForNodelessEvents() {
        Event event = new Event();
        event.setUei("uei.opennms.org/traps/genericTrap");
        event.setSource("test-source");
        event.setTime(new Date());
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer).send(captor.capture());

        assertThat(captor.getValue().key()).isEqualTo(0L);
    }

    @Test
    public void shouldHandleSendNowWithLog() throws Exception {
        Event event1 = createEvent("uei.opennms.org/nodes/nodeDown", 1L);
        event1.setAlarmData(new AlarmData());
        // Pure IPC event (not in CROSS_CONTAINER_INTERNAL_UEIS, no alarmData)
        Event event2 = createEvent("uei.opennms.org/internal/provisiond/nodeScanCompleted", 2L);

        Log log = new Log();
        log.addEvent(event1);
        log.addEvent(event2);

        forwarder.sendNow(log);

        // event1 is FAULT -> fault topic, event2 is IPC -> IPC topic
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer, times(2)).send(captor.capture());

        List<ProducerRecord<Long, byte[]>> records = captor.getAllValues();
        assertThat(records.get(0).topic()).isEqualTo(FAULT_TOPIC);
        assertThat(records.get(1).topic()).isEqualTo(IPC_TOPIC);

        verify(eventExpander).process(any(Log.class));
        verify(tsidAssigner).process(any(Log.class));
    }

    @Test
    public void shouldHandleSendNowSync() {
        Event event = createEvent("uei.opennms.org/nodes/nodeDown", 5L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNowSync(event);

        verify(kafkaProducer).send(any());
    }

    @Test
    public void shouldHandleSendNowSyncWithLog() throws Exception {
        Event event = createEvent("uei.opennms.org/internal/reload", 3L);

        Log log = new Log();
        log.addEvent(event);

        forwarder.sendNowSync(log);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer).send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo(IPC_TOPIC);

        verify(eventExpander).process(any(Log.class));
    }

    private Event createEvent(String uei, long nodeId) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId);
        event.setSource("test-source");
        event.setTime(new Date());
        return event;
    }
}
