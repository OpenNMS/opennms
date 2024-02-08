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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmpKafkaProducer implements BmpMessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BmpKafkaProducer.class);

    private final String topicPrefix;
    private final KafkaProducer<String, String> producer;

    public BmpKafkaProducer(final String topicPrefix,
                            final Map<String, Object> kafkaConfig) {
        if (topicPrefix != null) {
            this.topicPrefix = String.format("%s.", topicPrefix);
        } else {
            this.topicPrefix = "";
        }

        this.producer = buildProducer(kafkaConfig);
    }

    private static KafkaProducer<String, String> buildProducer(final Map<String, Object> kafkaConfig) {
        // Apply some defaults
        kafkaConfig.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        kafkaConfig.putIfAbsent(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 1000000);
        kafkaConfig.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 2);
        kafkaConfig.putIfAbsent(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);

        return new KafkaProducer<>(kafkaConfig, new StringSerializer(), new StringSerializer());
    }

    @Override
    public void handle(Message message, Context context) {
        final StringBuffer buffer = new StringBuffer();
        message.serialize(buffer);

        final String topic = this.topicPrefix + message.getType().getTopic();
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.getCollectorHashId(), buffer.toString());

        this.producer.send(record, (meta, err) -> {
            if (err != null) {
                LOG.warn("Failed to send OpenBMP message", err);
            } else {
                LOG.trace("Send OpenBMP message: {} = {}@{}", meta.topic(), meta.offset(), meta.partition());
            }
        });
    }

    @Override
    public void close() {
        producer.close();
    }
}
