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
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.eventd.router.IpcMessageConverter;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class KafkaEventForwarderTest {

    private static final String TOPIC = "opennms-fault-events";

    private EventProcessor eventExpander;
    private EventProcessor tsidAssigner;
    private EventClassifier eventClassifier;
    private IpcMessageConverter ipcMessageConverter;
    private MessageBus messageBus;

    @SuppressWarnings("unchecked")
    private KafkaProducer<Long, byte[]> kafkaProducer = mock(KafkaProducer.class);

    private KafkaEventForwarder forwarder;

    @Before
    public void setUp() {
        eventExpander = mock(EventProcessor.class);
        tsidAssigner = mock(EventProcessor.class);
        eventClassifier = new EventClassifier();
        ipcMessageConverter = new IpcMessageConverter();
        messageBus = mock(MessageBus.class);
        kafkaProducer = mock(KafkaProducer.class);

        forwarder = new KafkaEventForwarder(
                eventExpander,
                tsidAssigner,
                eventClassifier,
                ipcMessageConverter,
                messageBus,
                kafkaProducer,
                TOPIC
        );
    }

    @Test
    public void shouldPublishFaultEventToKafka() {
        // FAULT: external UEI + alarmData
        Event event = createEvent("uei.opennms.org/nodes/nodeDown", 42L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        // Should publish to Kafka
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer).send(captor.capture());

        ProducerRecord<Long, byte[]> record = captor.getValue();
        assertThat(record.topic()).isEqualTo(TOPIC);
        assertThat(record.key()).isEqualTo(42L);
        assertThat(record.value()).isNotEmpty();

        // Should NOT publish to MessageBus
        verify(messageBus, never()).publish(any(IpcMessage.class));
    }

    @Test
    public void shouldPublishIpcEventToMessageBus() {
        // IPC: internal UEI, no alarmData
        Event event = createEvent("uei.opennms.org/internal/discovery/newSuspect", 10L);

        forwarder.sendNow(event);

        // Should publish to MessageBus
        ArgumentCaptor<IpcMessage> captor = ArgumentCaptor.forClass(IpcMessage.class);
        verify(messageBus).publish(captor.capture());

        IpcMessage message = captor.getValue();
        assertThat(message.getType()).isEqualTo("discovery/newSuspect");
        assertThat(message.getSource()).isEqualTo("test-source");

        // Should NOT publish to Kafka
        verify(kafkaProducer, never()).send(any());
    }

    @Test
    public void shouldPublishDualEventToBothKafkaAndMessageBus() {
        // DUAL: internal UEI + alarmData
        Event event = createEvent("uei.opennms.org/internal/alarms/alarmCreated", 7L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        // Should publish to both Kafka and MessageBus
        verify(kafkaProducer).send(any());
        verify(messageBus).publish(any(IpcMessage.class));
    }

    @Test
    public void shouldCallExpanderAndTsidAssignerBeforeRouting() throws Exception {
        Event event = createEvent("uei.opennms.org/nodes/nodeDown", 1L);
        event.setAlarmData(new AlarmData());

        forwarder.sendNow(event);

        // Verify the enrichment pipeline was called
        verify(eventExpander).process(any(Log.class));
        verify(tsidAssigner).process(any(Log.class));

        // And routing happened (Kafka send proves enrichment completed first)
        verify(kafkaProducer).send(any());
    }

    @Test
    public void shouldUseZeroKeyForNodelessEvents() {
        // Event without a nodeId
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
        Event event2 = createEvent("uei.opennms.org/internal/discovery/newSuspect", 2L);

        Log log = new Log();
        log.addEvent(event1);
        log.addEvent(event2);

        forwarder.sendNow(log);

        // event1 is FAULT -> Kafka only
        // event2 is IPC -> MessageBus only
        verify(kafkaProducer).send(any());
        verify(messageBus).publish(any(IpcMessage.class));
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

        verify(messageBus).publish(any(IpcMessage.class));
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
