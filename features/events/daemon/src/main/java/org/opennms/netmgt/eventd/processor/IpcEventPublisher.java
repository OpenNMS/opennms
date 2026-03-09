package org.opennms.netmgt.eventd.processor;

import java.util.Objects;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes IPC events to the {@code opennms-ipc-events} Kafka topic.
 * All events passed to this processor are published without filtering
 * (the caller — {@code EventRouter} — has already classified them as IPC).
 */
public class IpcEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(IpcEventPublisher.class);

    private final KafkaProducer<Long, byte[]> producer;
    private final String topicName;
    private final Function<Event, byte[]> eventSerializer;

    public IpcEventPublisher(KafkaProducer<Long, byte[]> producer,
                             String topicName,
                             Function<Event, byte[]> eventSerializer) {
        this.producer = Objects.requireNonNull(producer);
        this.topicName = Objects.requireNonNull(topicName);
        this.eventSerializer = Objects.requireNonNull(eventSerializer);
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            byte[] value = eventSerializer.apply(event);
            Long key = event.getNodeid() != null ? event.getNodeid() : 0L;
            producer.send(new ProducerRecord<>(topicName, key, value));
            LOG.debug("Published IPC event {} (TSID={}) to Kafka topic {}",
                    event.getUei(), event.getDbid(), topicName);
        }
    }
}
