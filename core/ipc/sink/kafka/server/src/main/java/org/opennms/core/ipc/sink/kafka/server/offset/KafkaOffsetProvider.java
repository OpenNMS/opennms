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

/**
 * Copyright 2016 Symantec Corporation.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Most of the functionality of this code has been derived from 
 * https://github.com/srotya/kafka-lag-monitor 
 * 
 */

package org.opennms.core.ipc.sink.kafka.server.offset;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.OnmsKafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class KafkaOffsetProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOffsetProvider.class);

    // 120 retries is equivalent to 1 minute
    private static final int NUM_RETRIES = 120;

    private static final int INVALID = -1;

    private KafkaOffsetConsumerRunner consumerRunner;

    private final Properties kafkaConfig = new Properties();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-offset-consumer-%d")
            .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);

    private static final Map<String, KafkaConsumer> consumerMap = new HashMap<String, KafkaConsumer>();

    private Map<String, Map<Integer, KafkaOffset>> consumerOffsetMap = new ConcurrentHashMap<>();

    private Map<String, Long> consumerLagMap = new ConcurrentHashMap<>();

    private MetricRegistry  kafkaOffsetMetrics = new MetricRegistry();

    private JmxReporter reporter = null;

    private final AtomicInteger resetBroker = new AtomicInteger(0);

    private int partitionNumber = INVALID;

    private HostAndPort kafkaHost;

    private final KafkaConfigProvider configProvider;

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
                                    KafkaConsumer con = getConsumer();
                                    long realOffset = getLastOffset(con, struct.getString(KafkaOffsetConstants.TOPIC),
                                            partition, -1);
                                    long consumerOffset = readOffsetMessageValue(
                                            ByteBuffer.wrap(consumerRecord.value()));
                                    long lag = 0;
                                    if (realOffset > 0) {
                                        lag = realOffset - consumerOffset;
                                    }
                                    KafkaOffset mon = new KafkaOffset(group, topic, partition, realOffset,
                                            consumerOffset, lag);
                                    LOGGER.trace("group : {} , topic: {}:{} , offsets : {}-{}-{}", group, topic,
                                            partition, consumerOffset, realOffset, lag);

                                    Map<Integer, KafkaOffset> map = consumerOffsetMap.get(topic);

                                    if (map == null) {
                                        map = new ConcurrentHashMap<>();
                                        consumerOffsetMap.put(topic, map);
                                        kafkaOffsetMetrics.register(MetricRegistry.name(topic, "Lag"), new Gauge<Long>() {
                                            @Override
                                            public Long getValue() {
                                                return consumerLagMap.get(topic);

                                            }
                                        });
                                    }
                                    map.put(partition, mon);
                                    long totalLag = 0;
                                    for (KafkaOffset offset : map.values()) {
                                        totalLag += offset.getLag();
                                    }
                                    LOGGER.trace(" Total lag for topic {} is {} ", topic, totalLag);

                                    consumerLagMap.put(topic, totalLag);

                                } catch (Exception e) {
                                    LOGGER.trace("Exception while getting offset", e);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.trace("Exception while getting offset", e);
            } finally {
                consumer.close();
            }

        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            LOGGER.info("Closing offset consumer");
            consumer.wakeup();
        }

    }

    public KafkaOffsetProvider() {
        this(new OnmsKafkaConfigProvider(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX));
    }

    public KafkaOffsetProvider(KafkaConfigProvider configProvider) {
        this.configProvider = Objects.requireNonNull(configProvider);
    }

    private long readOffsetMessageValue(ByteBuffer buffer) {
        buffer.getShort(); // read and ignore version
        long offset = buffer.getLong();
        return offset;
    }

    public KafkaConsumer getConsumer() {
        Object kafkaServer = null;
        if (kafkaHost == null) {
            kafkaServer = kafkaConfig.get("bootstrap.servers");
            if (kafkaServer == null || !(kafkaServer instanceof String)) {
                throw new IllegalArgumentException("Kafka server is invalid");
            }
            final String kafkaString = (String) kafkaServer;
            kafkaHost = HostAndPort.fromString(kafkaString);
            if (kafkaHost == null) {
                throw new IllegalArgumentException("Kafka server is invalid");
            }
        }
        if (resetBroker.get() == NUM_RETRIES) {
            LOGGER.trace(" Max num of retries reached, try if there is another broker");
            kafkaHost = HostAndPort.getNextHostAndPort(kafkaHost);
            if (kafkaHost == null) {
                // No valid kafkaHost, shutdown offset consumer.
                consumerRunner.shutdown();
                throw new IllegalArgumentException("Kafka server is invalid");
            }
            partitionNumber = INVALID;
            resetBroker.set(0);
        }
        return getConsumer(kafkaHost.getHost(), kafkaHost.getPort());
    }

    public KafkaConsumer getConsumer(String host, int port) {

        KafkaConsumer consumer = consumerMap.get(host + ":" + port);
        if (consumer == null) {
            Properties localKafkaConfig = kafkaConfig;
            localKafkaConfig.put("bootstrap.servers", host + ":" + port);
            localKafkaConfig.put("session.timeout.ms", KafkaOffsetConstants.TIMEOUT);
            localKafkaConfig.putAll(configProvider.getProperties());
            consumer = new KafkaConsumer(localKafkaConfig);
            LOGGER.info("Created a new Kafka Consumer with host  {}:{} ", host, port);
            consumerMap.put(host + ":" + port, consumer);
        }
        return consumer;
    }

    public Map<String, Map<Integer, KafkaOffset>> getConsumerOffsetMap() {
        return consumerOffsetMap;
    }

    public long getLastOffset(KafkaConsumer consumer, String topic, int partition, long whichTime) {
        long lastOffset = 0;
        try {
            Set<TopicPartition> partitions = new HashSet<TopicPartition>();
            TopicPartition actualTopicPartition = new TopicPartition(topic, partition);
            partitions.add(actualTopicPartition);
            lastOffset = (long)consumer.endOffsets(partitions).get(actualTopicPartition);
        } catch (Exception e) {
            LOGGER.trace("Error while collecting the log Size for topic: {}:{} ", topic, partition, e);
            // Store first partitionNumber and track errors with that partition
            if (partitionNumber == INVALID) {
                partitionNumber = partition;
            }
            if (partitionNumber == partition) {
                resetBroker.getAndIncrement();
            }
        }
        return lastOffset;
    }

    public void closeConnection() throws InterruptedException {
        for (Map.Entry<String, KafkaConsumer> consumer : consumerMap.entrySet()) {
            LOGGER.info("Closing connection for: " + consumer.getKey());
            consumer.getValue().close();
        }
    }

    public void start() {
        kafkaConfig.clear();
        kafkaConfig.put("enable.auto.commit", "false");
        kafkaConfig.put("auto.offset.reset", "latest");
        kafkaConfig.put("key.deserializer", ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.putAll(configProvider.getProperties());
        consumerRunner = Utils.runWithGivenClassLoader(() -> new KafkaOffsetConsumerRunner(), KafkaConsumer.class.getClassLoader());
        reporter = JmxReporter.forRegistry(kafkaOffsetMetrics).inDomain("org.opennms.core.ipc.sink.kafka").build();

        reporter.start();
        executor.execute(consumerRunner);
    }

    public void stop() throws InterruptedException {
        reporter.stop();
        consumerRunner.shutdown();
        closeConnection();
    }
}
