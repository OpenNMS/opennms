/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.collection;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.features.kafka.producer.OpennmsKafkaProducer;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaPersisterFactory implements PersisterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersisterFactory.class);
    private CollectionSetMapper collectionSetMapper;

    private KafkaProducer<String, byte[]> producer;
    private ConfigurationAdmin configAdmin;
    private String topicName;

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        return createPersister(params, repository);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        KafkaPersister persister = new KafkaPersister();
        persister.setCollectionSetMapper(collectionSetMapper);
        persister.setProducer(producer);
        persister.setTopicName(topicName);
        return persister;
    }

    public void init() throws IOException {
        // Create the Kafka producer
        final Properties producerConfig = new Properties();
        final Dictionary<String, Object> properties = configAdmin
                .getConfiguration(OpennmsKafkaProducer.KAFKA_CLIENT_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                producerConfig.put(key, properties.get(key));
            }
        }
        // Overwrite the serializers
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());

        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Class-loader hack for accessing the org.apache.kafka.common.serialization.*
            Thread.currentThread().setContextClassLoader(null);
            producer = new KafkaProducer<>(producerConfig);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
        LOG.info(" kafka producer initialized with {} ", producerConfig);
    }

    public void destroy() {
        if (producer != null) {
            LOG.info(" close kafka producer");
            producer.close();
            producer = null;
        }
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void setCollectionSetMapper(CollectionSetMapper collectionSetMapper) {
        this.collectionSetMapper = collectionSetMapper;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

}
