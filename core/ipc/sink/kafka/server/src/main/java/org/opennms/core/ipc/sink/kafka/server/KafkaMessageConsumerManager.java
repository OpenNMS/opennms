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

import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.DEFAULT_MESSAGEID_CONFIG;
import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.MESSAGEID_CACHE_CONFIG;
import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_CONSUMER_DOMAIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.util.GlobalTracer;

public class KafkaMessageConsumerManager extends AbstractMessageConsumerManager implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageConsumerManager.class);

    private final Map<SinkModule<?, Message>, List<KafkaConsumerRunner>> consumerRunnersByModule = new ConcurrentHashMap<>();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("kafka-consumer-%d")
            .build();

    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    private final Properties kafkaConfig = new Properties();
    private final KafkaConfigProvider configProvider;
    // Cache that stores chunks in large message.
    private Cache<String, ByteString> largeMessageCache;
    private Cache<String, Integer> currentChunkCache;

    private MetricRegistry metricRegistry;
    private JmxReporter jmxReporter;

    @Autowired
    private TracerRegistry tracerRegistry;

    @Autowired
    private Identity identity;


    public KafkaMessageConsumerManager() {
        this(new OnmsKafkaConfigProvider(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX));
    }

    public KafkaMessageConsumerManager(KafkaConfigProvider configProvider, Identity identity,
                                       TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        this.configProvider = Objects.requireNonNull(configProvider);
        this.identity = identity;
        this.tracerRegistry = tracerRegistry;
        this.metricRegistry = metricRegistry;
    }
    public KafkaMessageConsumerManager(KafkaConfigProvider configProvider) {
        this.configProvider = Objects.requireNonNull(configProvider);
    }


    private class KafkaConsumerRunner implements Runnable {
        private final SinkModule<?, Message> module;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer<String, byte[]> consumer;
        private final String topic;

        private Histogram messageSize;
        private Timer dispatchTime;

        public KafkaConsumerRunner(SinkModule<?, Message> module) {
            this.module = module;
            
            final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX, module.getId());
            topic = topicNameFactory.getName();

            consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaConsumer.class.getClassLoader());
            messageSize = getMetricRegistry().histogram(MetricRegistry.name(module.getId(), METRIC_MESSAGE_SIZE));
            dispatchTime = getMetricRegistry().timer(MetricRegistry.name(module.getId(), METRIC_DISPATCH_TIME));

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
                            // Parse sink message content from protobuf.
                            SinkMessageProtos.SinkMessage sinkMessage = SinkMessageProtos.SinkMessage.parseFrom(record.value());
                            byte[] messageInBytes = sinkMessage.getContent().toByteArray();
                            String messageId = sinkMessage.getMessageId();
                            // Handle large message where there are multiple chunks of message.
                            if (sinkMessage.getTotalChunks() > 1) {

                                if (largeMessageCache == null || currentChunkCache == null) {
                                    LOG.error("LargeMessageCache config {}={} is invalid", MESSAGEID_CACHE_CONFIG,
                                            kafkaConfig.getProperty(MESSAGEID_CACHE_CONFIG));
                                    continue;
                                }
                                // Avoid duplicate chunks. discard if chunk is repeated.
                                if(currentChunkCache.getIfPresent(messageId) == null) {
                                    currentChunkCache.put(messageId, 0);
                                }
                                Integer chunkNum = currentChunkCache.getIfPresent(messageId);
                                if(chunkNum != null && chunkNum == sinkMessage.getCurrentChunkNumber()) {
                                    continue;
                                }
                                ByteString byteString = largeMessageCache.getIfPresent(messageId);
                                if(byteString != null) {
                                    largeMessageCache.put(messageId, byteString.concat(sinkMessage.getContent()));
                                } else {
                                    largeMessageCache.put(messageId, sinkMessage.getContent());
                                }
                                currentChunkCache.put(messageId, ++chunkNum);
                                // continue till all chunks arrive.
                                if (sinkMessage.getTotalChunks() != chunkNum) {
                                    continue;
                                }
                                byteString = largeMessageCache.getIfPresent(messageId);
                                if (byteString != null) {
                                    messageInBytes = byteString.toByteArray();
                                    largeMessageCache.invalidate(messageId);
                                    currentChunkCache.invalidate(messageId);
                                } else {
                                    continue;
                                }
                            }
                            // Update metrics.
                            messageSize.update(messageInBytes.length);
                            Tracer.SpanBuilder spanBuilder = buildSpanFromSinkMessage(sinkMessage);
                            // Tracing scope and Metrics Timer context will measure the time to dispatch.
                            try(Scope scope = spanBuilder.startActive(true);
                                Timer.Context context = dispatchTime.time()) {
                                scope.span().setTag(TracerConstants.TAG_MESSAGE_SIZE, messageInBytes.length);
                                scope.span().setTag(TracerConstants.TAG_TOPIC, topic);
                                scope.span().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
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

        private Tracer.SpanBuilder buildSpanFromSinkMessage(SinkMessageProtos.SinkMessage sinkMessage) {

            Tracer tracer = getTracer();
            Tracer.SpanBuilder spanBuilder;
            Map<String, String> tracingInfoMap = new HashMap<>();
            sinkMessage.getTracingInfoList().forEach(tracingInfo -> {
                tracingInfoMap.put(tracingInfo.getKey(), tracingInfo.getValue());
            });
            SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfoMap));
            if (context != null) {
                // Span on consumer side will follow the span from producer (minion).
                spanBuilder = tracer.buildSpan(module.getId()).addReference(References.FOLLOWS_FROM, context);
            } else {
                spanBuilder = tracer.buildSpan(module.getId());
            }
            return spanBuilder;
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
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
        String cacheConfig = kafkaConfig.getProperty(MESSAGEID_CACHE_CONFIG, DEFAULT_MESSAGEID_CONFIG);
        largeMessageCache =  CacheBuilder.from(cacheConfig).build();
        currentChunkCache = CacheBuilder.from(cacheConfig).build();
        if (identity != null && tracerRegistry != null) {
            tracerRegistry.init(identity.getId());
        }
        jmxReporter = JmxReporter.forRegistry(getMetricRegistry()).
                inDomain(SINK_METRIC_CONSUMER_DOMAIN).build();
        jmxReporter.start();
    }

    public void shutdown() {
        executor.shutdown();
        if (jmxReporter != null) {
            jmxReporter.close();
        }
        if(getStartupExecutor() != null) {
            getStartupExecutor().shutdown();
        }
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public Tracer getTracer() {
        if (tracerRegistry != null) {
            return tracerRegistry.getTracer();
        }
        return GlobalTracer.get();
    }

    public MetricRegistry getMetricRegistry() {
        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
        }
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
}
