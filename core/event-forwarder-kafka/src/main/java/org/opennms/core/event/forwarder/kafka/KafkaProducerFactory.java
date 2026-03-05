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
package org.opennms.core.event.forwarder.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;

/**
 * Factory for creating {@link KafkaProducer} instances configured for the
 * event-forwarder-kafka module.
 *
 * <p>Uses {@link LongSerializer} for keys (event node IDs) and
 * {@link ByteArraySerializer} for values (XML-serialized event payloads).</p>
 */
public class KafkaProducerFactory {

    private KafkaProducerFactory() {
        // static factory — prevent instantiation
    }

    /**
     * Builds Kafka producer properties for the given bootstrap servers.
     *
     * @param bootstrapServers comma-separated list of Kafka broker addresses
     * @return configured {@link Properties} for a {@link KafkaProducer}
     */
    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }

    /**
     * Creates a new {@link KafkaProducer} connected to the given bootstrap servers.
     *
     * @param bootstrapServers comma-separated list of Kafka broker addresses
     * @return a new {@link KafkaProducer} instance; caller is responsible for closing it
     */
    public static KafkaProducer<Long, byte[]> create(String bootstrapServers) {
        return new KafkaProducer<>(buildProperties(bootstrapServers));
    }
}
