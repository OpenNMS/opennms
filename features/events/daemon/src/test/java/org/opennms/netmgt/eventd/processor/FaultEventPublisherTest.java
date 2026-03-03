package org.opennms.netmgt.eventd.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class FaultEventPublisherTest {

    private static final byte[] SERIALIZED_BYTES = "protobuf-encoded".getBytes(StandardCharsets.UTF_8);

    private KafkaProducer<Long, byte[]> mockProducer;
    private Function<Event, byte[]> eventSerializer;
    private FaultEventPublisher publisher;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        mockProducer = mock(KafkaProducer.class);
        eventSerializer = event -> SERIALIZED_BYTES;
        publisher = new FaultEventPublisher(mockProducer, "opennms-fault-events", eventSerializer);
    }

    @Test
    public void shouldPublishFaultEventToKafka() throws Exception {
        Event event = createFaultEvent("uei.opennms.org/nodes/nodeDown", 42L);
        Log log = createLog(event);

        publisher.process(log);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        ProducerRecord<Long, byte[]> record = captor.getValue();
        assertThat(record.topic()).isEqualTo("opennms-fault-events");
        assertThat(record.key()).isEqualTo(42L);
        assertThat(record.value()).isEqualTo(SERIALIZED_BYTES);
    }

    @Test
    public void shouldSkipEventsWithoutAlarmData() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/internal/reloadDaemonConfig");
        Log log = createLog(event);

        publisher.process(log);

        verify(mockProducer, never()).send(any());
    }

    @Test
    public void shouldUseZeroKeyForNodelessEvents() throws Exception {
        Event event = createFaultEvent("uei.opennms.org/threshold/highThreshold", null);
        Log log = createLog(event);

        publisher.process(log);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(0L);
    }

    private Event createFaultEvent(String uei, Long nodeId) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId != null ? nodeId : 0L);
        event.setDbid(123456789L);
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey(uei + ":" + nodeId);
        alarmData.setAlarmType(1);
        event.setAlarmData(alarmData);
        return event;
    }

    private Log createLog(Event... events) {
        Events eventsContainer = new Events();
        for (Event e : events) {
            eventsContainer.addEvent(e);
        }
        Log log = new Log();
        log.setEvents(eventsContainer);
        return log;
    }
}
