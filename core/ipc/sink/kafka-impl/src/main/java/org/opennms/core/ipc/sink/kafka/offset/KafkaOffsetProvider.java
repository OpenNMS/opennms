/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.sink.kafka.offset;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.kafka.KafkaSinkConstants;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;

public class KafkaOffsetProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOffsetProvider.class);

    private KafkaOffsetConsumerRunner consumerRunner;

    private final Properties kafkaConfig = new Properties();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-offset-consumer-%d")
            .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);

    private static final Map<String, SimpleConsumer> consumerMap = new HashMap<String, SimpleConsumer>();

    private Map<String, Map<String, KafkaOffset>> consumerOffsetMap = new ConcurrentHashMap<>();

    private class KafkaOffsetConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer<byte[], byte[]> consumer;

        public KafkaOffsetConsumerRunner() {
            consumer = new KafkaConsumer<>(kafkaConfig);
        }

        @Override
        public void run() {
            try {
                Logging.putPrefix(MessageConsumerManager.LOG_PREFIX);
                consumer.subscribe(Arrays.asList(KafkaOffsetConstants.OFFSETS_TOPIC));
                LOGGER.info("Connected to Kafka consumer offset topic");
                Schema schema = new Schema(new Field("group", Schema.STRING),
                        new Field(KafkaOffsetConstants.TOPIC, Schema.STRING), new Field("partition", Schema.INT32));
                while (!closed.get()) {
                    ConsumerRecords<byte[], byte[]> records = consumer.poll(KafkaOffsetConstants.POLL_INTERVAL);
                    for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
                        if (consumerRecord.value() != null && consumerRecord.key() != null) {
                            ByteBuffer key = ByteBuffer.wrap(consumerRecord.key());
                            short version = key.getShort();
                            if (version < 2) {
                                try {
                                    Struct struct = (Struct) schema.read(key);
                                    String group = struct.getString(KafkaOffsetConstants.GROUP);
                                    if ( !group.equals(SystemInfoUtils.getInstanceId())) {
                                        continue;
                                    }
                                    String topic = struct.getString(KafkaOffsetConstants.TOPIC);
                                    int partition = struct.getInt(KafkaOffsetConstants.PARTITION);
                                    SimpleConsumer con = getConsumer();
                                    long realOffset = getLastOffset(con, struct.getString(KafkaOffsetConstants.TOPIC),
                                            partition, -1);
                                    long consumerOffset = readOffsetMessageValue(
                                            ByteBuffer.wrap(consumerRecord.value()));
                                    long lag = realOffset - consumerOffset;
                                    KafkaOffset mon = new KafkaOffset(group, topic, partition, realOffset,
                                            consumerOffset, lag);
                                    LOGGER.info("group : {} , topic: {}:{} , offsets : {}-{}-{}", group, topic,
                                            partition, consumerOffset, realOffset, lag);
                                    Map<String, KafkaOffset> map = consumerOffsetMap.get(topic);
                                    if (map == null) {
                                        map = new ConcurrentHashMap<>();
                                        consumerOffsetMap.put(topic, map);
                                    }
                                    map.put(group + "%" + partition, mon);

                                } catch (Exception e) {
                                    LOGGER.error("Exception while getting offset", e);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception while getting offset", e);
            } finally {
                consumer.close();
            }

        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }

    }

    private long readOffsetMessageValue(ByteBuffer buffer) {
        buffer.getShort(); // read and ignore version
        long offset = buffer.getLong();
        return offset;
    }

    public SimpleConsumer getConsumer() {
        Object kafkaServer = kafkaConfig.get("bootstrap.servers");
        if (kafkaServer == null || !(kafkaServer instanceof String)) {
            throw new IllegalArgumentException("Kafka server is invalid");
        }
        final String kafkaString = (String) kafkaServer;
        HostAndPort kafkaHost = HostAndPort.fromString(kafkaString);
        if (kafkaHost == null) {
            throw new IllegalArgumentException("Kafka server is invalid");
        }
        return getConsumer(kafkaHost.getHost(), kafkaHost.getPort());
    }

    public SimpleConsumer getConsumer(String host, int port) {

        SimpleConsumer consumer = consumerMap.get(host);
        if (consumer == null) {
            consumer = new SimpleConsumer(host, port, KafkaOffsetConstants.TIMEOUT, KafkaOffsetConstants.BUFFERSIZE,
                    KafkaOffsetConstants.CLIENT_NAME);
            LOGGER.info("Created a new Kafka Consumer for host: " + host);
            consumerMap.put(host, consumer);
        }
        return consumer;
    }

    public Map<String, Map<String, KafkaOffset>> getNewConsumer() {
        return consumerOffsetMap;
    }

    public long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime) {
        long lastOffset = 0;
        try {
            List<String> topics = Collections.singletonList(topic);
            TopicMetadataRequest req = new TopicMetadataRequest(topics);
            kafka.javaapi.TopicMetadataResponse topicMetadataResponse = consumer.send(req);
            TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
            for (TopicMetadata topicMetadata : topicMetadataResponse.topicsMetadata()) {
                for (PartitionMetadata partitionMetadata : topicMetadata.partitionsMetadata()) {
                    if (partitionMetadata.partitionId() == partition) {
                        String partitionHost = partitionMetadata.leader().host();
                        consumer = getConsumer(partitionHost, partitionMetadata.leader().port());
                        break;
                    }
                }
            }
            Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
            requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
            kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo,
                    kafka.api.OffsetRequest.CurrentVersion(), KafkaOffsetConstants.CLIENT_NAME);
            OffsetResponse response = consumer.getOffsetsBefore(request);
            if (response.hasError()) {
                LOGGER.error("Error fetching Offset Data from the Broker. Reason: {}",
                        response.errorCode(topic, partition));
                lastOffset = 0;
            }
            long[] offsets = response.offsets(topic, partition);
            lastOffset = offsets[0];
        } catch (Exception e) {
            LOGGER.error("Error while collecting the log Size for topic: {}:{} ", topic, partition, e);
        }
        return lastOffset;
    }

    public void closeConnection() throws InterruptedException {
        for (SimpleConsumer consumer : consumerMap.values()) {
            LOGGER.info("Closing connection for: " + consumer.host());
            consumer.close();
        }
    }

    public void start() {
        // Set the defaults
        kafkaConfig.clear();
        kafkaConfig.put("group.id", SystemInfoUtils.getInstanceId());
        kafkaConfig.put("enable.auto.commit", "false");
        kafkaConfig.put("auto.offset.reset", "latest");
        kafkaConfig.put("key.deserializer", ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());

        // Find all of the system properties that start with
        // 'org.opennms.core.ipc.sink.kafka.'
        // and add them to the config. See
        // https://kafka.apache.org/0100/documentation.html#newconsumerconfigs
        // for the list of supported properties
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null || !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String) keyAsObject;

            if (key.length() > KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX.length()
                    && key.startsWith(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX)) {
                final String kafkaConfigKey = key.substring(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX.length());
                kafkaConfig.put(kafkaConfigKey, entry.getValue());
            }
        }
        consumerRunner = new KafkaOffsetConsumerRunner();
        executor.execute(consumerRunner);
    }

    public void stop() throws InterruptedException {
        consumerRunner.shutdown();
        closeConnection();
    }
}
