package org.opennms.netmgt.eventd.consumer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Consumes events from a Kafka topic and broadcasts them to core's local
 * {@link EventIpcBroadcaster} listeners.
 *
 * <p>Events originated by core itself (detected via TSID node-id) are skipped
 * to prevent echo loops — core publishes events to the same topics via
 * {@link org.opennms.netmgt.eventd.processor.FaultEventPublisher} and
 * {@link org.opennms.netmgt.eventd.processor.IpcEventPublisher}.</p>
 *
 * <p>Two instances are wired in {@code applicationContext-eventDaemon.xml}:
 * one for the fault topic and one for the IPC topic.</p>
 */
public class KafkaEventConsumer implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventConsumer.class);

    /** TSID node-id occupies bits 12-21 (10 bits, shifted right by SEQUENCE_BITS=12). */
    private static final int TSID_NODE_SHIFT = 12;
    private static final long TSID_NODE_MASK = 0x3FFL;

    private final String bootstrapServers;
    private final String topicName;
    private final String groupId;
    private final int coreNodeId;
    private final EventIpcBroadcaster localBroadcaster;

    private KafkaConsumer<Long, byte[]> consumer;
    private volatile boolean running;
    private Thread pollThread;

    public KafkaEventConsumer(String bootstrapServers,
                              String topicName,
                              String groupId,
                              int coreNodeId,
                              EventIpcBroadcaster localBroadcaster) {
        this.bootstrapServers = bootstrapServers;
        this.topicName = topicName;
        this.groupId = groupId;
        this.coreNodeId = coreNodeId;
        this.localBroadcaster = localBroadcaster;
    }

    @Override
    public void afterPropertiesSet() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        consumer = new KafkaConsumer<>(props, new LongDeserializer(), new ByteArrayDeserializer());
        consumer.subscribe(Collections.singletonList(topicName));

        running = true;
        pollThread = new Thread(this::pollLoop, "kafka-event-consumer-" + topicName);
        pollThread.setDaemon(true);
        pollThread.start();

        LOG.info("KafkaEventConsumer started: topic={}, group={}, coreNodeId={}",
                topicName, groupId, coreNodeId);
    }

    @Override
    public void destroy() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
        if (pollThread != null) {
            try {
                pollThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (consumer != null) {
            consumer.close();
        }
        LOG.info("KafkaEventConsumer stopped: topic={}", topicName);
    }

    private void pollLoop() {
        while (running) {
            try {
                ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Long, byte[]> record : records) {
                    processRecord(record);
                }
            } catch (RecordDeserializationException e) {
                LOG.warn("Skipping undeserializable record at offset {}", e.offset(), e);
                consumer.seek(e.topicPartition(), e.offset() + 1);
            } catch (org.apache.kafka.common.errors.WakeupException e) {
                if (running) {
                    LOG.warn("Unexpected wakeup", e);
                }
            } catch (Exception e) {
                LOG.error("Error in Kafka event poll loop for topic {}", topicName, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processRecord(ConsumerRecord<Long, byte[]> record) {
        try {
            String xml = new String(record.value(), StandardCharsets.UTF_8);
            Event event = JaxbUtils.unmarshal(Event.class,
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            if (isFromCore(event)) {
                LOG.trace("Skipping core-originated event: uei={} dbid={}", event.getUei(), event.getDbid());
                return;
            }

            LOG.debug("Broadcasting Kafka event from topic {}: uei={} dbid={} source={}",
                    topicName, event.getUei(), event.getDbid(), event.getSource());
            localBroadcaster.broadcastNow(event, false);
        } catch (Exception e) {
            LOG.error("Failed to process Kafka record at offset {}", record.offset(), e);
        }
    }

    /**
     * Checks whether the event was originated by this core instance by
     * extracting the node-id from the TSID stored in {@code event.getDbid()}.
     *
     * <p>TSID layout: {@code [timestamp (42 bits)][nodeId (10 bits)][sequence (12 bits)]}</p>
     */
    private boolean isFromCore(Event event) {
        if (event.getDbid() != null && event.getDbid() > 0) {
            long tsid = event.getDbid();
            int nodeId = (int) ((tsid >> TSID_NODE_SHIFT) & TSID_NODE_MASK);
            return nodeId == coreNodeId;
        }
        return false;
    }
}
