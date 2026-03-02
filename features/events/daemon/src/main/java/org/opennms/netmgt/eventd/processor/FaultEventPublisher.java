package org.opennms.netmgt.eventd.processor;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FaultEventPublisher.class);

    private final KafkaProducer<Long, byte[]> producer;
    private final String topicName;

    public FaultEventPublisher(KafkaProducer<Long, byte[]> producer, String topicName) {
        this.producer = producer;
        this.topicName = topicName;
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
        try {
            StringWriter writer = new StringWriter();
            JaxbUtils.marshal(event, writer);
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event: " + event.getUei(), e);
        }
    }
}
