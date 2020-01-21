/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;

public class BmpIntegrationAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpIntegrationAdapter.class);

    private final KafkaProducer<String, String> producer;

    public BmpIntegrationAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
        this.producer = buildProducer(adapterConfig);
    }

    @Override
    public void handleMessage(final TelemetryMessageLogEntry message,
                              final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray());

        if (!getString(document, "@type")
                .map(type -> Header.Type.PEER_UP_NOTIFICATION.name().equals(type) ||
                             Header.Type.PEER_DOWN_NOTIFICATION.name().equals(type))
                .orElse(false)) {
            return;
        }

    }

    @Override
    public void destroy() {
        this.producer.close();
        super.destroy();
    }

    private void send(final Message message) {
        final StringBuffer buffer = new StringBuffer();
        message.serialize(buffer);

        final String topic = message.getType().getTopic();
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, buffer.toString());

        this.producer.send(record);
    }

    private static KafkaProducer<String, String> buildProducer(final AdapterDefinition adapterConfig) {
        final Map<String, Object> kafkaConfig = Maps.newHashMap();
        for (final Map.Entry<String, String> entry : adapterConfig.getParameterMap().entrySet()) {
            StringUtils.truncatePrefix(entry.getKey(), "kafka.").ifPresent(key -> {
                kafkaConfig.put(key, entry.getValue());
            });
        }

        // TODO fooker: Apply defaults (steal from https://github.com/SNAS/openbmp/blob/1a615a3c75a0143cc87ec70458471f0af67d3929/Server/src/kafka/MsgBusImpl_kafka.cpp#L162)

        return new KafkaProducer<>(kafkaConfig, new StringSerializer(), new StringSerializer());
    }
}
