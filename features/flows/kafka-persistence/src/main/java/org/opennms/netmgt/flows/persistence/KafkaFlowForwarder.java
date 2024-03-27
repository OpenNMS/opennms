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
package org.opennms.netmgt.flows.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.flows.persistence.model.FlowDocument;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.util.JsonFormat;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class KafkaFlowForwarder implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFlowForwarder.class);
    public static final String KAFKA_CLIENT_PID = "org.opennms.features.flows.persistence.kafka";
    private final ConfigurationAdmin configAdmin;
    private KafkaProducer<String, byte[]> producer;
    private String topicName;
    private boolean useJson = false;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .omittingInsignificantWhitespace();
    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(1).every(Duration.ofSeconds(60))
            .build();
    private Properties producerConfig;

    private final Meter forwarded;
    private final Meter persisted;
    private final Counter skipped;
    private final Counter failed;

    public KafkaFlowForwarder(final ConfigurationAdmin configAdmin,
                              final MetricRegistry metricRegistry) {
        this.configAdmin = configAdmin;

        this.forwarded = metricRegistry.meter("forwarded");
        this.persisted = metricRegistry.meter("persisted");
        this.skipped = metricRegistry.counter("skipped");
        this.failed = metricRegistry.counter("failed");
    }

    @Override
    public void persist(Collection<? extends Flow> flows) {
        for (final var enrichedFlow: flows) {
            this.forwarded.mark();

            if (this.producer == null) {
                this.skipped.inc();
                RATE_LIMITED_LOG.warn("Kafka Producer is not configured for flow forwarding.");
                return;
            }

            try {
                FlowDocument flowDocument = FlowDocumentBuilder.buildFlowDocument(enrichedFlow);
                final byte[] payload;
                if (useJson) {
                    payload = jsonPrinter.print(flowDocument).getBytes(StandardCharsets.UTF_8);
                } else {
                    payload = flowDocument.toByteArray();
                }

                final ProducerRecord<String, byte[]> record = new ProducerRecord<>(this.topicName, payload);
                this.producer.send(record, (recordMetadata, e) -> {
                    if (e != null) {
                        this.failed.inc();
                        RATE_LIMITED_LOG.warn("Failed to send flow document to kafka: {}.", record, e);
                    } else if (LOG.isTraceEnabled()) {
                        this.persisted.mark();
                        LOG.trace("Persisted flow document {} to kafka.", flowDocument);
                    }
                });
            } catch (Exception e) {
                LOG.error("Exception while sending flow to kafka.", e);
            }
        }
    }

    public void init() throws IOException {
        // Create the Kafka producer
        producerConfig = new Properties();
        final Dictionary<String, Object> properties = configAdmin
                .getConfiguration(KAFKA_CLIENT_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                producerConfig.put(key, properties.get(key));
            }
        }

        if (producerConfig.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) != null) {
            // Overwrite the serializers
            producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
            // Class-loader hack for accessing the kafka classes when initializing producer.
            producer = runWithGivenClassLoader(() -> new KafkaProducer<>(producerConfig), KafkaProducer.class.getClassLoader());
            LOG.info("Kafka Producer initialized with config {}", producerConfig);
        }
    }

    public void destroy() {
        if (producer != null) {
            LOG.info("Closed Kafka Producer");
            producer.close();
            producer = null;
        }
    }

    private static <T> T runWithGivenClassLoader(final Supplier<T> supplier, ClassLoader classLoader) {
        Objects.requireNonNull(supplier);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setUseJson(boolean useJson) {
        this.useJson = useJson;
    }
}
