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
 * Publishes fault events (events with alarm data) to a Kafka topic using
 * Protobuf serialization.
 *
 * <p>The serializer function is injected at construction time, allowing the
 * Karaf blueprint to wire in {@code ProtobufMapper} without creating a
 * compile-time dependency cycle between events-daemon and kafka-producer.</p>
 */
public class FaultEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FaultEventPublisher.class);

    private final KafkaProducer<Long, byte[]> producer;
    private final String topicName;
    private final Function<Event, byte[]> eventSerializer;

    public FaultEventPublisher(KafkaProducer<Long, byte[]> producer,
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
            if (!isFaultEvent(event)) {
                LOG.debug("Skipping non-fault event: {}", event.getUei());
                continue;
            }
            byte[] value = serializeEvent(event);
            Long key = event.getNodeid() != null ? event.getNodeid() : 0L;
            producer.send(new ProducerRecord<>(topicName, key, value));
            LOG.debug("Published fault event {} (TSID={}) to Kafka topic {}",
                    event.getUei(), event.getDbid(), topicName);
        }
    }

    private boolean isFaultEvent(Event event) {
        return event.getAlarmData() != null;
    }

    private byte[] serializeEvent(Event event) {
        return eventSerializer.apply(event);
    }
}
