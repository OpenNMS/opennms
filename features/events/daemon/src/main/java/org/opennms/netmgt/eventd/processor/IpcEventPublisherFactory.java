package org.opennms.netmgt.eventd.processor;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;

/**
 * Factory for creating {@link IpcEventPublisher} wired to a Kafka producer.
 */
public class IpcEventPublisherFactory {

    private IpcEventPublisherFactory() {
    }

    public static IpcEventPublisher create(String bootstrapServers, String topicName) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        KafkaProducer<Long, byte[]> producer =
                new KafkaProducer<>(props, new LongSerializer(), new ByteArraySerializer());
        return new IpcEventPublisher(producer, topicName, new XmlEventSerializer());
    }
}
