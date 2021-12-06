/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.kafka.publisher;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaTwinConstants;
import org.opennms.core.ipc.common.kafka.OnmsKafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.twin.api.TwinStrategy;
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequest;
import org.opennms.core.ipc.twin.common.TwinUpdate;
import org.opennms.core.ipc.twin.kafka.common.KafkaConsumerRunner;
import org.opennms.core.ipc.twin.kafka.common.Topic;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX;

public class KafkaTwinPublisher extends AbstractTwinPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTwinPublisher.class);

    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final KafkaConfigProvider kafkaConfigProvider;

    private KafkaProducer<String, byte[]> producer;
    private KafkaConsumerRunner consumerRunner;

    public KafkaTwinPublisher(final LocalTwinSubscriber localTwinSubscriber, TracerRegistry tracerRegistry) {
        this(localTwinSubscriber, new OnmsKafkaConfigProvider(KafkaTwinConstants.KAFKA_CONFIG_SYS_PROP_PREFIX, KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX), tracerRegistry);
    }

    public KafkaTwinPublisher(final LocalTwinSubscriber localTwinSubscriber,
                              final KafkaConfigProvider kafkaConfigProvider,
                              final TracerRegistry tracerRegistry) {
        super(localTwinSubscriber, tracerRegistry);
        this.kafkaConfigProvider = Objects.requireNonNull(kafkaConfigProvider);
    }

    public void init() throws IOException {
        final var kafkaConfig = new Properties();
        kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaConfig.putAll(kafkaConfigProvider.getProperties());
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            LOG.debug("Initialized kafka twin publisher with {}", kafkaConfig);
            this.producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(kafkaConfig), KafkaProducer.class.getClassLoader());

            final KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaProducer.class.getClassLoader());
            consumer.subscribe(ImmutableList.<String>builder()
                    .add(Topic.request())
                    .build());

            this.consumerRunner = new KafkaConsumerRunner(consumer, this::handleMessage, "twin-publisher");
        }
    }

    public void close() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            if (this.consumerRunner != null) {
                this.consumerRunner.close();
            }
            if (this.producer != null) {
                this.producer.close();
            }
        }
    }

    @Override
    protected void handleSinkUpdate(final TwinUpdate sinkUpdate) {
        try {
            final var topic = Strings.isNullOrEmpty(sinkUpdate.getLocation())
                    ? Topic.responseGlobal()
                    : Topic.responseForLocation(sinkUpdate.getLocation());

            final var proto = mapTwinResponse(sinkUpdate);

            final var record = new ProducerRecord<>(topic, sinkUpdate.getKey(), proto.toByteArray());
            this.producer.send(record, (meta, ex) -> {
                if (ex != null) {
                    RATE_LIMITED_LOG.error("Error publishing update", ex);
                }
            });
        } catch (Exception e) {
            LOG.error("Exception while sending update for key {} at location {} ", sinkUpdate.getKey(), sinkUpdate.getLocation());
        }
    }

    private void handleMessage(final ConsumerRecord<String, byte[]> record) {
        try {
            final TwinRequest request = mapTwinRequestProto(record.value());
            String tracingOperationKey = generateTracingOperationKey(request.getLocation(), request.getKey());
            Tracer.SpanBuilder spanBuilder = TracingInfoCarrier.buildSpanFromTracingMetadata(getTracer(),
                    tracingOperationKey, request.getTracingInfo(), References.FOLLOWS_FROM);
            try (Scope scope = spanBuilder.startActive(true)) {
                final var response = this.getTwin(request);
                addTracingInfo(scope.span(), response);
                this.handleSinkUpdate(response);
            }
        } catch (Exception e) {
            LOG.error("Exception while processing request", e);
        }
    }
}
