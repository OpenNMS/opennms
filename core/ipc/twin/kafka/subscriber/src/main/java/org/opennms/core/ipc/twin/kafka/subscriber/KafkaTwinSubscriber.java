/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.ipc.twin.kafka.subscriber;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.swrve.ratelimitedlogger.RateLimitedLog;
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
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.twin.common.AbstractTwinSubscriber;
import org.opennms.core.ipc.twin.api.TwinRequest;
import org.opennms.core.ipc.twin.api.TwinUpdate;
import org.opennms.core.ipc.twin.kafka.common.KafkaConsumerRunner;
import org.opennms.core.ipc.twin.kafka.common.Topic;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public class KafkaTwinSubscriber extends AbstractTwinSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTwinSubscriber.class);

    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final KafkaConfigProvider kafkaConfigProvider;

    private KafkaProducer<String, byte[]> producer;
    private KafkaConsumerRunner consumer;

    public KafkaTwinSubscriber(final MinionIdentity identity,
                               final KafkaConfigProvider kafkaConfigProvider,
                               final TracerRegistry tracerRegistry,
                               final MetricRegistry metricRegistry) {
        super(identity, tracerRegistry, metricRegistry);
        this.kafkaConfigProvider = Objects.requireNonNull(kafkaConfigProvider);
    }

    public void init() throws Exception {
        final var kafkaConfig = new Properties();
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getIdentity().getId());
        kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaConfig.putAll(kafkaConfigProvider.getProperties());

        LOG.debug("Initialized kafka twin subscriber with {}", kafkaConfig);
        this.producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(kafkaConfig), KafkaProducer.class.getClassLoader());

        final KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaProducer.class.getClassLoader());
        consumer.subscribe(ImmutableList.<String>builder()
                                        .add(Topic.responseForLocation(this.getIdentity().getLocation()))
                                        .add(Topic.responseGlobal())
                                        .build());

        this.consumer = new KafkaConsumerRunner(consumer, this::handleMessage, "twin-subscriber");
    }

    public void close() throws IOException {
        super.close();

        if (this.consumer != null) {
            this.consumer.close();
        }

        if (this.producer != null) {
            this.producer.close();
        }
    }

    @Override
    protected void sendRpcRequest(final TwinRequest twinRequest) {

        try {
            TwinRequestProto twinRequestProto = mapTwinRequestToProto(twinRequest);
            final var record = new ProducerRecord<>(Topic.request(), twinRequest.getKey(), twinRequestProto.toByteArray());
            this.producer.send(record, (meta, ex) -> {
                if (ex != null) {
                    RATE_LIMITED_LOG.error("Error sending request", ex);
                }
            });
        } catch (Exception e) {
            LOG.error("Exception while sending request with key {}", twinRequest.getKey());
        }
    }

    private void handleMessage(final ConsumerRecord<String, byte[]> record) {
        try {
            final TwinUpdate response = mapTwinResponseToProto(record.value());
            this.accept(response);
        } catch (Exception e) {
            LOG.error("Exception while processing twin update", e);
        }
    }
}
