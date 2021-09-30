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
