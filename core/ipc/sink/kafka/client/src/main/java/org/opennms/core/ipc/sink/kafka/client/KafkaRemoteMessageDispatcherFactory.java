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

package org.opennms.core.ipc.sink.kafka.client;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<String> {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaRemoteMessageDispatcherFactory.class);

    private final Properties kafkaConfig = new Properties();

    private ConfigurationAdmin configAdmin;

    private BundleContext bundleContext;

    private KafkaProducer<String,byte[]> producer;

    @Override
    public <S extends Message, T extends Message> String getModuleMetadata(final SinkModule<S, T> module) {
        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX, module.getId());
        return topicNameFactory.getName();
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);
            final ProducerRecord<String,byte[]> record = new ProducerRecord<>(topic, module.marshal(message));
            try {
                // From KafkaProducer's JavaDoc: The producer is thread safe and should generally be shared among all threads for best performance.
                final Future<RecordMetadata> future = producer.send(record);
                // The call to dispatch() is synchronous, so we block until the message was sent
                future.get();
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while sending message to topic {}.", topic, e);
            } catch (ExecutionException e) {
                LOG.error("Error occured while sending message to topic {}.", topic, e);
            }
        }
    }

    public void init() throws IOException {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            // Defaults
            kafkaConfig.clear();
            kafkaConfig.put("key.serializer", StringSerializer.class.getCanonicalName());
            kafkaConfig.put("value.serializer", ByteArraySerializer.class.getCanonicalName());
            // Retrieve all of the properties from org.opennms.core.ipc.sink.kafka.cfg
            KafkaConfigProvider configProvider = new OsgiKafkaConfigProvider(KafkaSinkConstants.KAFKA_CONFIG_PID, configAdmin);
            kafkaConfig.putAll(configProvider.getProperties());
            LOG.info("KafkaRemoteMessageDispatcherFactory: initializing the Kafka producer with: {}", kafkaConfig);
            producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(kafkaConfig), KafkaProducer.class.getClassLoader());
            onInit();
        }
    }

    public void destroy() {
        onDestroy();
        if (producer != null) {
            producer.close();
            producer = null;
        }
    }

    @Override
    public String getMetricDomain() {
        return KafkaLocalMessageDispatcherFactory.class.getPackage().getName();
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
