package org.opennms.netmgt.eventd.processor;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;

/**
 * Factory for creating a {@link FaultEventPublisher} wired to a Kafka producer.
 *
 * <p>This factory exists in {@code events-daemon} to avoid a circular Maven
 * dependency with {@code event-forwarder-kafka} (which already depends on
 * {@code events-daemon} for {@link org.opennms.netmgt.eventd.router.EventClassifier}).</p>
 */
public class FaultEventPublisherFactory {

    private FaultEventPublisherFactory() {
    }

    /**
     * Creates a {@link FaultEventPublisher} with a new Kafka producer.
     *
     * @param bootstrapServers Kafka broker addresses
     * @param topicName        Kafka topic for fault events
     * @return a configured FaultEventPublisher
     */
    public static FaultEventPublisher create(String bootstrapServers, String topicName) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        KafkaProducer<Long, byte[]> producer =
                new KafkaProducer<>(props, new LongSerializer(), new ByteArraySerializer());
        return new FaultEventPublisher(producer, topicName, new XmlEventSerializer());
    }
}
