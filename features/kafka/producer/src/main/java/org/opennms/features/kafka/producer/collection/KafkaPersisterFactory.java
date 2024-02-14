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
package org.opennms.features.kafka.producer.collection;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.ipc.common.kafka.Utils;
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
        KafkaPersister persister = new KafkaPersister(params);
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
        // Class-loader hack for accessing the kafka classes when initializing producer.
        producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(producerConfig), KafkaProducer.class.getClassLoader());
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
