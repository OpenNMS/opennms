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

package org.opennms.core.ipc.sink.kafka.server;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_CONSUMER_DOMAIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.OnmsKafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.ipc.sink.model.SinkMessageProtos;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public class KafkaMessageConsumerManager extends AbstractMessageConsumerManager implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageConsumerManager.class);

    private final Map<SinkModule<?, Message>, List<KafkaConsumerRunner>> consumerRunnersByModule = new ConcurrentHashMap<>();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("kafka-consumer-%d")
            .build();

    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    private final Properties kafkaConfig = new Properties();
    private final KafkaConfigProvider configProvider;

    private class KafkaConsumerRunner implements Runnable {
        private final SinkModule<?, Message> module;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer<String, byte[]> consumer;
        private final String topic;
        private final MetricRegistry metricRegistry = new MetricRegistry();
        private JmxReporter jmxReporter = null;
        private Meter messagesReceived;
        private Histogram messageSize;
        private Timer dispatchTime;
        public KafkaConsumerRunner(SinkModule<?, Message> module) {
            this.module = module;
            
            final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX, module.getId());
            topic = topicNameFactory.getName();

            consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaConsumer.class.getClassLoader());
            jmxReporter = JmxReporter.forRegistry(metricRegistry).
                    inDomain(SINK_METRIC_CONSUMER_DOMAIN).build();
            jmxReporter.start();
            messagesReceived = metricRegistry.meter(MetricRegistry.name(module.getId(), METRIC_MESSAGES_RECEIVED));
            messageSize = metricRegistry.histogram(MetricRegistry.name(module.getId(), METRIC_MESSAGE_SIZE));
            dispatchTime = metricRegistry.timer(MetricRegistry.name(module.getId(), METRIC_DISPATCH_TIME));

        }

        @Override
        public void run() {
            Logging.putPrefix(MessageConsumerManager.LOG_PREFIX);
            try {
                consumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(100);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        try {
                            byte[] messageInBytes = getSinkMessageFromProto(record.value());
                            // Update metrics.
                            messagesReceived.mark();
                            messageSize.update(messageInBytes.length);
                            try (Timer.Context context = dispatchTime.time()) {
                                dispatch(module, module.unmarshal(messageInBytes));
                            }
                        } catch (RuntimeException e) {
                            LOG.warn("Unexpected exception while dispatching message", e);
                        } catch (InvalidProtocolBufferException e) {
                            LOG.warn("Error parsing procotol buffer in message. The message will be dropped. \n" +
                                    "Ensure that all components are running the same version of the software.");
                        }
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
            }
        }

        private byte[] getSinkMessageFromProto(byte[] value) throws InvalidProtocolBufferException {
            SinkMessageProtos.SinkMessage sinkMessage = SinkMessageProtos.SinkMessage.parseFrom(value);
            return sinkMessage.getContent().toByteArray();
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
            jmxReporter.close();
        }
    }

    public KafkaMessageConsumerManager() {
        this(new OnmsKafkaConfigProvider(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX));
    }

    public KafkaMessageConsumerManager(KafkaConfigProvider configProvider) {
        this.configProvider = Objects.requireNonNull(configProvider);
    }

    @Override
    protected void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (!consumerRunnersByModule.containsKey(module)) {
            LOG.info("Starting consumers for module: {}", module);

            final int numConsumerThreads = getNumConsumerThreads(module);
            final List<KafkaConsumerRunner> consumerRunners = new ArrayList<>(numConsumerThreads);
            for (int i = 0; i < numConsumerThreads; i++) {
                final KafkaConsumerRunner consumerRunner = new KafkaConsumerRunner(module);
                executor.execute(consumerRunner);
                consumerRunners.add(new KafkaConsumerRunner(module));
            }

            consumerRunnersByModule.put(module, consumerRunners);
        }
    }

    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (consumerRunnersByModule.containsKey(module)) {
            LOG.info("Stopping consumers for module: {}", module);
            final List<KafkaConsumerRunner> consumerRunners = consumerRunnersByModule.get(module);
            for (KafkaConsumerRunner consumerRunner : consumerRunners) {
                consumerRunner.shutdown();
            }
            consumerRunnersByModule.remove(module);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        kafkaConfig.clear();
        kafkaConfig.put("enable.auto.commit", "true");
        kafkaConfig.put("key.deserializer", StringDeserializer.class.getCanonicalName());
        kafkaConfig.put("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put("auto.commit.interval.ms", "1000");
        kafkaConfig.putAll(configProvider.getProperties()); // e.g. groupId, and such
        LOG.info("KafkaMessageConsumerManager: consuming from Kafka using: {}", kafkaConfig);
    }
}
