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

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;

/**
 * Factory for creating {@link KafkaConsumer} instances configured for the
 * event-forwarder-kafka module.
 *
 * <p>Uses {@link LongDeserializer} for keys (event node IDs) and
 * {@link ByteArrayDeserializer} for values (XML-serialized event payloads).</p>
 */
public class KafkaConsumerFactory {

    private KafkaConsumerFactory() {
        // static factory — prevent instantiation
    }

    /**
     * Builds Kafka consumer properties for the given bootstrap servers and consumer group.
     *
     * @param bootstrapServers comma-separated list of Kafka broker addresses
     * @param groupId          the consumer group ID
     * @return configured {@link Properties} for a {@link KafkaConsumer}
     */
    public static Properties buildProperties(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return props;
    }

    /**
     * Creates a new {@link KafkaConsumer} connected to the given bootstrap servers.
     *
     * @param bootstrapServers comma-separated list of Kafka broker addresses
     * @param groupId          the consumer group ID
     * @return a new {@link KafkaConsumer} instance; caller is responsible for closing it
     */
    public static KafkaConsumer<Long, byte[]> create(String bootstrapServers, String groupId) {
        return new KafkaConsumer<>(buildProperties(bootstrapServers, groupId));
    }
}
