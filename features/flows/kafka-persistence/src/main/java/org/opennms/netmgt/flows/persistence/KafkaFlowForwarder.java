/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.persistence;

import java.io.IOException;
import java.time.Duration;
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
import org.opennms.netmgt.flows.api.EnrichedFlow;
import org.opennms.netmgt.flows.api.EnrichedFlowForwarder;
import org.opennms.netmgt.flows.persistence.model.FlowDocument;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class KafkaFlowForwarder implements EnrichedFlowForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFlowForwarder.class);
    public static final String KAFKA_CLIENT_PID = "org.opennms.features.flows.persistence.kafka";
    private final ConfigurationAdmin configAdmin;
    private KafkaProducer<String, byte[]> producer;
    private String topicName;
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
    public void forward(EnrichedFlow enrichedFlow) {
        this.forwarded.mark();

        if (producer == null) {
            this.skipped.inc();
            RATE_LIMITED_LOG.warn("Kafka Producer is not configured for flow forwarding.");
            return;
        }

        try {
            FlowDocument flowDocument = FlowDocumentBuilder.buildFlowDocument(enrichedFlow);
            final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, flowDocument.toByteArray());
            producer.send(record, (recordMetadata, e) -> {
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
}
