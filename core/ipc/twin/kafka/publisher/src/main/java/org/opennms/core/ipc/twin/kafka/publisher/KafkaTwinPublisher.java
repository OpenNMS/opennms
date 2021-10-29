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
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequestBean;
import org.opennms.core.ipc.twin.common.TwinResponseBean;
import org.opennms.core.ipc.twin.kafka.common.KafkaConsumerRunner;
import org.opennms.core.ipc.twin.kafka.common.Topic;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class KafkaTwinPublisher extends AbstractTwinPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTwinPublisher.class);

    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final KafkaConfigProvider kafkaConfigProvider;

    private KafkaProducer<String, byte[]> producer;
    private KafkaConsumerRunner consumerRunner;

    public KafkaTwinPublisher(final LocalTwinSubscriber localTwinSubscriber) {
        this(localTwinSubscriber, new OnmsKafkaConfigProvider(KafkaTwinConstants.KAFKA_CONFIG_SYS_PROP_PREFIX));
    }

    public KafkaTwinPublisher(final LocalTwinSubscriber localTwinSubscriber,
                              final KafkaConfigProvider kafkaConfigProvider) {
        super(localTwinSubscriber);
        this.kafkaConfigProvider = Objects.requireNonNull(kafkaConfigProvider);
    }

    public void init() throws IOException {
        final var kafkaConfig = new Properties();
        kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaConfig.putAll(kafkaConfigProvider.getProperties());

        LOG.debug("Initialized kafka twin publisher with {}", kafkaConfig);
        this.producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(kafkaConfig), KafkaProducer.class.getClassLoader());

        final KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaProducer.class.getClassLoader());
        consumer.subscribe(ImmutableList.<String>builder()
                                        .add(Topic.request())
                                        .build());

        this.consumerRunner = new KafkaConsumerRunner(consumer, this::handleMessage, "twin-publisher");
    }

    public void close() throws IOException {
        super.close();

        if (this.consumerRunner != null) {
            this.consumerRunner.close();
        }

        if (this.producer != null) {
            this.producer.close();
        }
    }

    @Override
    protected void handleSinkUpdate(final TwinResponseBean sinkUpdate) {
        final var topic = Strings.isNullOrEmpty(sinkUpdate.getLocation())
                ? Topic.responseGlobal()
                : Topic.responseForLocation(sinkUpdate.getLocation());

        final var proto = TwinResponseProto.newBuilder();
        proto.setConsumerKey(sinkUpdate.getKey());
        if (!Strings.isNullOrEmpty(sinkUpdate.getLocation())) {
            proto.setLocation(sinkUpdate.getLocation());
        }
        if (sinkUpdate.getObject() != null) {
            proto.setTwinObject(ByteString.copyFrom(sinkUpdate.getObject()));
        }

        final var record = new ProducerRecord<>(topic, sinkUpdate.getKey(), proto.build().toByteArray());
        this.producer.send(record, (meta, ex) -> {
            if (ex != null) {
                RATE_LIMITED_LOG.error("Error publishing update", ex);
            }
        });
        this.producer.flush();
    }

    private void handleMessage(final ConsumerRecord<String, byte[]> record) {
        final TwinRequestBean request;
        try {
            final var proto = TwinRequestProto.parseFrom(record.value());

            request = new TwinRequestBean();
            request.setKey(proto.getConsumerKey());
            request.setLocation(proto.getLocation());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Failed to parse protobuf for the request", e);
            return;
        }

        final var response = this.getTwin(request);
        this.handleSinkUpdate(response);
    }
}
